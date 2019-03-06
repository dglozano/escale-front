package com.dglozano.escale.ui.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.content.SharedPreferences;
import android.util.Log;

import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.repository.ChatRepository;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.ui.Event;
import com.dglozano.escale.util.Constants;
import com.dglozano.escale.util.SharedPreferencesLiveData;

import java.util.NoSuchElementException;
import java.util.Objects;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class MainActivityViewModel extends ViewModel {

    private PatientRepository mPatientRepository;
    private ChatRepository mChatRepository;
    private final LiveData<Event<Boolean>> mMustChangePassword;
    private final LiveData<Integer> mNumberOfUnreadMessages;
    private final LiveData<Integer> mNumberOfUnreadMessagesSharedPref;
    private final LiveData<Patient> mLoggedPatient;
    private final MutableLiveData<Integer> positionOfCurrentFragment;
    private final SharedPreferences mSharedPreferences;
    private final CompositeDisposable disposables;

    @Inject
    public MainActivityViewModel(PatientRepository patientRepository,
                                 SharedPreferences sharedPreferences,
                                 ChatRepository chatRepository) {
        disposables = new CompositeDisposable();
        mChatRepository = chatRepository;
        mSharedPreferences = sharedPreferences;
        positionOfCurrentFragment = new MutableLiveData<>();
        mPatientRepository = patientRepository;
        mLoggedPatient = mPatientRepository.getLoggedPatient();
        mMustChangePassword = Transformations.map(mLoggedPatient,
                patient -> new Event<>(patient != null && !patient.hasChangedDefaultPassword())
        );
        mNumberOfUnreadMessagesSharedPref = new SharedPreferencesLiveData.SharedPreferenceIntLiveData(
                mSharedPreferences,
                Constants.UNREAD_MESSAGES_SHARED_PREF, 0);
        mNumberOfUnreadMessages = Transformations.map(mNumberOfUnreadMessagesSharedPref, unreadMsg -> {
            if (positionOfCurrentFragment.getValue() != null
                    && positionOfCurrentFragment.getValue() == 3
                    && unreadMsg > 0) {
                markMessagesAsRead();
                return 0;
            }
            return unreadMsg;
        });
    }

    public LiveData<Integer> getPositionOfCurrentFragment() {
        return positionOfCurrentFragment;
    }

    public void setPositionOfCurrentFragment(int positionOfCurrentFragment) {
        this.positionOfCurrentFragment.postValue(positionOfCurrentFragment);
    }

    public LiveData<Patient> getLoggedPatient() {
        return mLoggedPatient;
    }

    public LiveData<Event<Boolean>> watchMustChangePassword() {
        return mMustChangePassword;
    }

    public void handleMustChangePasswordEvent() {
        Objects.requireNonNull(mMustChangePassword.getValue()).handleContent();
    }

    public void logout() {
        mPatientRepository.logout();
    }

    public LiveData<Integer> getNumberOfUnreadMessages() {
        return mNumberOfUnreadMessages;
    }


    public LiveData<String> getFirebaseToken() {
        return mPatientRepository.getFirebaseDeviceToken();
    }

    public Boolean isFirebaseTokenSent() {
        return mPatientRepository.isFirebaseTokenSent();
    }

    public Long getLoggedPatientId() {
        return mPatientRepository.getLoggedPatiendId();
    }

    public void markMessagesAsRead() {
        mSharedPreferences.edit().putInt(Constants.UNREAD_MESSAGES_SHARED_PREF, 0).apply();
    }

    public void addUnreadMessages(int newUnread) {
        int currentUnread = mSharedPreferences.getInt(Constants.UNREAD_MESSAGES_SHARED_PREF, 0);
        mSharedPreferences.edit().putInt(Constants.UNREAD_MESSAGES_SHARED_PREF, currentUnread + newUnread).apply();
    }

    public void refreshMessages() {
        disposables.add(mChatRepository
                .refreshMessagesAndCountOfPatientWithId(mPatientRepository.getLoggedPatiendId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::addUnreadMessages, error -> {
                    if(error instanceof NoSuchElementException) {
                        markMessagesAsRead();
                    } else {
                        Timber.e(error);
                    }
                })
        );
    }
}