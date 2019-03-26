package com.dglozano.escale.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.content.SharedPreferences;

import com.dglozano.escale.db.EscaleDatabase;
import com.dglozano.escale.db.dao.DoctorDao;
import com.dglozano.escale.db.dao.PatientDao;
import com.dglozano.escale.db.dao.UserDao;
import com.dglozano.escale.db.entity.AppUser;
import com.dglozano.escale.db.entity.Doctor;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.di.annotation.ApplicationScope;
import com.dglozano.escale.di.annotation.RootFileDirectory;
import com.dglozano.escale.exception.BadCredentialsException;
import com.dglozano.escale.exception.ChangePasswordException;
import com.dglozano.escale.exception.NotAPatientException;
import com.dglozano.escale.util.AppExecutors;
import com.dglozano.escale.util.Constants;
import com.dglozano.escale.util.FileUtil;
import com.dglozano.escale.util.SharedPreferencesLiveData;
import com.dglozano.escale.web.EscaleRestApi;
import com.dglozano.escale.web.dto.ChangePasswordDataDTO;
import com.dglozano.escale.web.dto.Credentials;
import com.dglozano.escale.web.dto.LoginResponse;

import java.io.File;
import java.util.Calendar;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import timber.log.Timber;

import static com.dglozano.escale.util.Constants.FRESH_TIMEOUT;
import static com.dglozano.escale.util.Constants.HAS_NEW_UNREAD_DIET_SHARED_PREF;
import static com.dglozano.escale.util.Constants.IS_FIREBASE_TOKEN_SENT_SHARED_PREF;
import static com.dglozano.escale.util.Constants.REFRESH_TOKEN_SHARED_PREF;
import static com.dglozano.escale.util.Constants.SCALE_USER_INDEX_SHARED_PREF;
import static com.dglozano.escale.util.Constants.SCALE_USER_PIN_SHARED_PREF;
import static com.dglozano.escale.util.Constants.TOKEN_SHARED_PREF;
import static com.dglozano.escale.util.Constants.UNREAD_MESSAGES_SHARED_PREF;

@ApplicationScope
public class PatientRepository {

    private PatientDao mPatientDao;
    private DoctorDao mDoctorDao;
    private UserDao mUserDao;
    private EscaleRestApi mEscaleRestApi;
    private AppExecutors mAppExecutors;
    private SharedPreferences mSharedPreferences;
    private LiveData<Long> mLoggedUserId;
    private LiveData<String> mFirebaseDeviceToken;
    private LiveData<Patient> mLoggedPatient;
    private EscaleDatabase mRoomDatabase;
    private File mRootFileDirectory;

    @Inject
    public PatientRepository(PatientDao patientDao, EscaleRestApi escaleRestApi, DoctorDao doctorDao,
                             UserDao userDao, AppExecutors executors, SharedPreferences sharedPreferences,
                             EscaleDatabase roomDatabase, @RootFileDirectory File rootFileDirectory) {
        mRootFileDirectory = rootFileDirectory;
        mUserDao = userDao;
        mDoctorDao = doctorDao;
        mPatientDao = patientDao;
        mRoomDatabase = roomDatabase;
        mEscaleRestApi = escaleRestApi;
        mAppExecutors = executors;
        mSharedPreferences = sharedPreferences;
        mLoggedUserId = new SharedPreferencesLiveData.SharedPreferenceLongLiveData(mSharedPreferences,
                Constants.LOGGED_USER_ID_SHARED_PREF, -1L);
        mFirebaseDeviceToken = new SharedPreferencesLiveData.SharedPreferenceStringLiveData(mSharedPreferences,
                Constants.FIREBASE_TOKEN_SHARED_PREF, "");
        mLoggedPatient = Transformations.switchMap(mLoggedUserId, this::getPatientById);
    }

    public LiveData<Patient> getPatientById(Long userId) {
        return mPatientDao.getPatientById(userId);
    }

    public Long getLoggedPatiendId() {
        return mSharedPreferences.getLong(Constants.LOGGED_USER_ID_SHARED_PREF, -1L);
    }


    public LiveData<Patient> getLoggedPatient() {
        return mLoggedPatient;
    }

    public Single<Patient> getLoggedPatientSingle() {
        return mPatientDao.getPatientSingleById(getLoggedPatiendId());
    }


    public LiveData<Long> getLoggedPatientIdAsLiveData() {
        return mLoggedUserId;
    }

    public Completable loginPatient(String email, String password) {
        return mEscaleRestApi.login(new Credentials(email, password)).flatMapCompletable(response -> {
            LoginResponse loginResponseBody = response.body();
            if (response.code() == 200 && loginResponseBody != null) {
                if (loginResponseBody.getUserType() != 2) {
                    // If the user had proper Credentials, but it is not a Patient,
                    // he can't use the mobile app.
                    throw new NotAPatientException();
                }
                Long loggedUserId = loginResponseBody.getId();
                String newToken = response.headers().get(Constants.TOKEN_HEADER_KEY);
                String newRefreshToken = response.headers().get(Constants.REFRESH_TOKEN_HEADER_KEY);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString(Constants.TOKEN_SHARED_PREF, newToken);
                editor.putString(Constants.REFRESH_TOKEN_SHARED_PREF, newRefreshToken);
                editor.putLong(Constants.LOGGED_USER_ID_SHARED_PREF, loggedUserId);
                editor.apply();
                return Completable.complete();
            } else {
                Timber.d("Login error - Resource code is not 200 (Bad Credentials)");
                throw new BadCredentialsException();
            }
        });
    }

    public Single<Long> changePassword(String currentPassword,
                                       String newPassword,
                                       String newPasswordRepeat) {
        Long userId = getLoggedPatientIdAsLiveData().getValue() == null ? -1L : getLoggedPatientIdAsLiveData().getValue();
        return mEscaleRestApi.changePassword(
                new ChangePasswordDataDTO(currentPassword,
                        newPassword, newPasswordRepeat), userId)
                .flatMap(changePasswordResponse -> {
                    if (changePasswordResponse.code() == 200) {
                        return mEscaleRestApi.getPatientById(userId);
                    } else {
                        Timber.d("Error change password, check");
                        throw new ChangePasswordException();
                    }
                })
                .flatMap(patientDTO -> {
                    Patient patient = new Patient(patientDTO, Calendar.getInstance().getTime());
                    return Single.fromCallable(() -> mPatientDao.save(patient));
                });
    }

    public Single<Long> refreshPatient(final Long userId) {
        return mPatientDao.hasUser(userId, FRESH_TIMEOUT)
                .map(freshInt -> freshInt == 1)
                .flatMapMaybe(hasFreshUser -> {
                    if (!hasFreshUser) {
                        return Maybe.fromSingle(mEscaleRestApi.getPatientById(userId));
                    } else {
                        return Maybe.empty();
                    }
                })
                .flatMapSingle(patientDTO -> {
                    Timber.d("Retrieved user with id %s from Api. Saving to db...", userId);
                    return Single.fromCallable(() -> {
                        Doctor doctor = new Doctor(patientDTO.getDoctorDTO(), Calendar.getInstance().getTime());
                        AppUser user = new AppUser(doctor);
                        mUserDao.save(user);
                        mDoctorDao.save(doctor);
                        Timber.d("Saving doctor with id %s ", doctor.getId());
                        return patientDTO;
                    });
                })
                .flatMap(patientDTO -> Single.fromCallable(() -> {
                    Patient patient = new Patient(patientDTO, Calendar.getInstance().getTime());
                    AppUser user = new AppUser(patient);
                    mUserDao.save(user);
                    return mPatientDao.save(patient);
                }));
    }

    public LiveData<String> getFirebaseDeviceToken() {
        return mFirebaseDeviceToken;
    }

    public Boolean isFirebaseTokenSent() {
        return mSharedPreferences.getBoolean(Constants.IS_FIREBASE_TOKEN_SENT_SHARED_PREF, false);
    }

    public void logout() {
        mAppExecutors.getDiskIO().execute(() -> {
            Timber.d("Clearing preferences");
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putLong(Constants.LOGGED_USER_ID_SHARED_PREF, -1L);
            editor.remove(TOKEN_SHARED_PREF);
            editor.remove(REFRESH_TOKEN_SHARED_PREF);
            editor.remove(SCALE_USER_INDEX_SHARED_PREF);
            editor.remove(SCALE_USER_PIN_SHARED_PREF);
            editor.putBoolean(IS_FIREBASE_TOKEN_SENT_SHARED_PREF, false);
            editor.putBoolean(HAS_NEW_UNREAD_DIET_SHARED_PREF, false);
            editor.putInt(UNREAD_MESSAGES_SHARED_PREF, 0);
            editor.apply();
            Timber.d("Clearing db");
            mRoomDatabase.clearAllTables();
            Timber.d("Clearing files");
            FileUtil.deleteContentOfRootDirectory(mRootFileDirectory);
        });
    }
}
