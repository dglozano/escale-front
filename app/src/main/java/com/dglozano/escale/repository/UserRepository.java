package com.dglozano.escale.repository;

import android.content.SharedPreferences;

import com.dglozano.escale.db.dao.PatientDao;
import com.dglozano.escale.db.dao.UserDao;
import com.dglozano.escale.di.annotation.ApplicationScope;
import com.dglozano.escale.exception.AccountDisabledException;
import com.dglozano.escale.exception.BadCredentialsException;
import com.dglozano.escale.exception.NotMobileAppUser;
import com.dglozano.escale.util.AppExecutors;
import com.dglozano.escale.util.Constants;
import com.dglozano.escale.util.LogoutHelper;
import com.dglozano.escale.util.SharedPreferencesLiveData;
import com.dglozano.escale.web.EscaleRestApi;
import com.dglozano.escale.web.dto.Credentials;
import com.dglozano.escale.web.dto.LoginResponse;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import io.reactivex.Completable;
import timber.log.Timber;

@ApplicationScope
public class UserRepository {

    private PatientDao mPatientDao;
    private DoctorRepository mDoctorRepository;
    private UserDao mUserDao;
    private EscaleRestApi mEscaleRestApi;
    private AppExecutors mAppExecutors;
    private LogoutHelper mLogoutHelper;
    private SharedPreferences mSharedPreferences;
    private LiveData<String> mFirebaseDeviceToken;

    @Inject
    public UserRepository(PatientDao patientDao, EscaleRestApi escaleRestApi,
                          DoctorRepository doctorRepository,
                          UserDao userDao, AppExecutors executors,
                          SharedPreferences sharedPreferences, LogoutHelper logoutHelper) {

        this.mLogoutHelper = logoutHelper;
        mUserDao = userDao;
        mDoctorRepository = doctorRepository;
        mPatientDao = patientDao;
        mEscaleRestApi = escaleRestApi;
        mAppExecutors = executors;
        mSharedPreferences = sharedPreferences;

        mFirebaseDeviceToken = new SharedPreferencesLiveData.SharedPreferenceStringLiveData(mSharedPreferences,
                Constants.FIREBASE_TOKEN_SHARED_PREF, "");
    }

    public Completable loginUser(String email, String password) {
        return mEscaleRestApi.login(new Credentials(email, password)).flatMapCompletable(response -> {
            LoginResponse loginResponseBody = response.body();
            if (response.code() == 200 && loginResponseBody != null) {
                if (loginResponseBody.getUserType() != 2 && loginResponseBody.getUserType() != 1) {
                    // If the user had proper Credentials, but it is not a Patient nor a Doctor
                    // he can't use the mobile app.
                    throw new NotMobileAppUser();
                }
                if (!loginResponseBody.isEnabled()) {
                    throw new AccountDisabledException();
                }
                Long loggedUserId = loginResponseBody.getId();
                String newToken = response.headers().get(Constants.TOKEN_HEADER_KEY);
                String newRefreshToken = response.headers().get(Constants.REFRESH_TOKEN_HEADER_KEY);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString(Constants.TOKEN_SHARED_PREF, newToken);
                editor.putString(Constants.REFRESH_TOKEN_SHARED_PREF, newRefreshToken);
                if (loginResponseBody.getUserType() == 1) {
                    // It's a doctor
                    editor.putLong(Constants.LOGGED_USER_ID_SHARED_PREF, -1);
                    editor.putLong(Constants.LOGGED_DOCTOR_ID_SHARED_PREF, loggedUserId);
                } else if (loginResponseBody.getUserType() == 2) {
                    // It's a patient
                    editor.putLong(Constants.LOGGED_USER_ID_SHARED_PREF, loggedUserId);
                    editor.putLong(Constants.LOGGED_DOCTOR_ID_SHARED_PREF, -1);
                }
                editor.apply();
                return Completable.complete();
            } else {
                Timber.d("Login error - Resource code is not 200 (Bad Credentials)");
                throw new BadCredentialsException();
            }
        });
    }

    public LiveData<String> getFirebaseDeviceToken() {
        return mFirebaseDeviceToken;
    }

    public Boolean isFirebaseTokenSent() {
        return mSharedPreferences.getBoolean(Constants.IS_FIREBASE_TOKEN_SENT_SHARED_PREF, false);
    }

    public void logout() {
        mLogoutHelper.logout();
    }

}
