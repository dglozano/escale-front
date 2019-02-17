package com.dglozano.escale.repository;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.content.SharedPreferences;

import com.dglozano.escale.db.dao.PatientDao;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.di.annotation.ApplicationScope;
import com.dglozano.escale.util.AppExecutors;
import com.dglozano.escale.util.BadCredentialsException;
import com.dglozano.escale.util.ChangePasswordException;
import com.dglozano.escale.util.Constants;
import com.dglozano.escale.util.NotAPatientException;
import com.dglozano.escale.util.SharedPreferencesLiveData;
import com.dglozano.escale.web.EscaleRestApi;
import com.dglozano.escale.web.dto.ChangePasswordDataDTO;
import com.dglozano.escale.web.dto.Credentials;
import com.dglozano.escale.web.dto.LoginResponse;

import java.util.Calendar;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.dglozano.escale.util.Constants.FRESH_TIMEOUT;

@ApplicationScope
public class PatientRepository {

    private PatientDao mPatientDao;
    private EscaleRestApi mEscaleRestApi;
    private AppExecutors mAppExecutors;
    private SharedPreferences mSharedPreferences;
    private LiveData<Long> mLoggedUserId;
    private LiveData<Patient> mLoggedPatient;

    @Inject
    public PatientRepository(PatientDao patientDao, EscaleRestApi escaleRestApi,
                             AppExecutors executors, SharedPreferences sharedPreferences) {
        mPatientDao = patientDao;
        mEscaleRestApi = escaleRestApi;
        mAppExecutors = executors;
        mSharedPreferences = sharedPreferences;
        mLoggedUserId = new SharedPreferencesLiveData.SharedPreferenceLongLiveData(mSharedPreferences,
                Constants.LOGGED_USER_ID_SHARED_PREF, -1L);
        mLoggedPatient = Transformations.switchMap(mLoggedUserId, this::getPatientById);
    }

    public LiveData<Patient> getPatientById(Long userId) {
        refreshPatient(userId);
        return mPatientDao.getPatientById(userId);
    }

    public LiveData<Patient> getLoggedPatient() {
        return mLoggedPatient;
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    private void refreshPatient(final Long userId) {
        mPatientDao.hasUser(userId, FRESH_TIMEOUT)
                .map(freshInt -> freshInt == 1)
                .flatMapMaybe(hasFreshUser -> {
                    if (!hasFreshUser) {
                        return Maybe.fromSingle(mEscaleRestApi.getPatientById(userId));
                    } else {
                        return Maybe.empty();
                    }
                })
                .flatMapSingle(patientDTO -> {
                    Timber.d("Retrieved user with id %s from Api", userId);
                    Patient patient = new Patient(patientDTO, Calendar.getInstance().getTime());
                    return Single.fromCallable(() -> mPatientDao.save(patient));
                })
                .subscribeOn(Schedulers.io())
                .subscribe(onSuccess -> Timber.d("Success refresh"), e -> {
                    if(e instanceof NoSuchElementException) {
                        Timber.d("Success refresh without calling Api");
                    } else {
                        Timber.e(e);
                    }
                });
    }

    public void logout() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putLong(Constants.LOGGED_USER_ID_SHARED_PREF, -1L);
        editor.clear();
        editor.apply();
    }

    public Long getLoggedPatiendId() {
        return mSharedPreferences.getLong(Constants.LOGGED_USER_ID_SHARED_PREF, -1L);
    }

}
