package com.dglozano.escale.ui.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.content.SharedPreferences;

import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.repository.ChatRepository;
import com.dglozano.escale.repository.DietRepository;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.ui.Event;
import com.dglozano.escale.util.Constants;

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
    private DietRepository mDietRepository;
    private LiveData<Event<Boolean>> mMustChangePassword;
    private LiveData<Integer> mNumberOfUnreadMessages;
    private final LiveData<Patient> mLoggedPatient;
    private final MutableLiveData<Integer> positionOfCurrentFragment;
    private final SharedPreferences mSharedPreferences;
    private final CompositeDisposable disposables;
    private MediatorLiveData<Boolean> mIsRefreshing;
    private MutableLiveData<Boolean> mIsRefreshingDiets;
    private MutableLiveData<Boolean> mIsRefreshingMessages;
    private MutableLiveData<Boolean> mIsRefreshingPatient;

    @Inject
    public MainActivityViewModel(PatientRepository patientRepository,
                                 SharedPreferences sharedPreferences,
                                 ChatRepository chatRepository, DietRepository dietRepository) {
        mDietRepository = dietRepository;
        disposables = new CompositeDisposable();
        mChatRepository = chatRepository;
        mSharedPreferences = sharedPreferences;
        positionOfCurrentFragment = new MutableLiveData<>();
        positionOfCurrentFragment.postValue(0);
        mPatientRepository = patientRepository;
        mLoggedPatient = mPatientRepository.getLoggedPatient();
        setupRefreshingObservable();
        setupMustChangePasswordObservable();
        setupUnreadMessagesObservable();
    }

    private void setupRefreshingObservable() {
        mIsRefreshing = new MediatorLiveData<>();
        mIsRefreshing.setValue(true);
        mIsRefreshingMessages = new MutableLiveData<>();
        mIsRefreshingPatient = new MutableLiveData<>();
        mIsRefreshingDiets = new MutableLiveData<>();
        mIsRefreshingMessages.setValue(false);
        mIsRefreshingPatient.setValue(false);
        mIsRefreshingDiets.setValue(false);

        mIsRefreshing.addSource(mIsRefreshingDiets, isRefreshingDiets ->
                mIsRefreshing.postValue(isRefreshingDiets ||
                        mIsRefreshingMessages.getValue() ||
                        mIsRefreshingPatient.getValue()));
        mIsRefreshing.addSource(mIsRefreshingMessages, isRefreshingMessages ->
                mIsRefreshing.postValue(isRefreshingMessages ||
                        mIsRefreshingDiets.getValue() ||
                        mIsRefreshingPatient.getValue()));
        mIsRefreshing.addSource(mIsRefreshingPatient, isRefreshingPatient ->
                mIsRefreshing.postValue(isRefreshingPatient ||
                        mIsRefreshingDiets.getValue() ||
                        mIsRefreshingMessages.getValue())
        );
    }

    public MediatorLiveData<Boolean> isRefreshing() {
        return mIsRefreshing;
    }

    private void setupMustChangePasswordObservable() {
        mMustChangePassword = Transformations.map(mLoggedPatient,
                patient -> new Event<>(patient != null && !patient.hasChangedDefaultPassword())
        );
    }

    private void setupUnreadMessagesObservable() {
        mNumberOfUnreadMessages = Transformations.map(mChatRepository.getNumberOfUnreadMessages(), unreadMsg -> {
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

    public void refreshData() {
        disposables.add(mPatientRepository.refreshPatient(mPatientRepository.getLoggedPatiendId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(d -> mIsRefreshingPatient.postValue(true))
                .subscribe(this::refreshEverythingElse,
                        error -> {
                            if (error instanceof NoSuchElementException) {
                                // Means that the user was fresh enough, so didn't have to call api
                                refreshEverythingElse(mPatientRepository.getLoggedPatiendId());
                            } else {
                                Timber.e(error);
                            }
                        })

        );
    }

    private void refreshEverythingElse(Long loggedPatientId) {
        mIsRefreshingDiets.postValue(true);
        mIsRefreshingMessages.postValue(true);
        mIsRefreshingPatient.postValue(false);
        refreshMessagesAndCount(loggedPatientId);
        refreshDiets(loggedPatientId);
    }

    private void refreshDiets(Long patientId) {
        disposables.add(mDietRepository.refreshDietsSingle(patientId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(numberOfNewDiets -> {
                            if (numberOfNewDiets != 0) {
                                markNewDietAsSeen(false);
                            }
                            mIsRefreshingDiets.postValue(false);
                        },
                        error -> {
                            Timber.e(error, "Error refreshing diets");
                            mIsRefreshingDiets.postValue(false);
                        })
        );
    }

    private void refreshMessagesAndCount(Long patientId) {
        disposables.add(mChatRepository.refreshMessagesAndCountOfPatientWithId(patientId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(newUnread -> {
                    mIsRefreshingMessages.postValue(false);
                    addUnreadMessages(newUnread);
                }, error -> {
                    if (error instanceof NoSuchElementException) {
                        markMessagesAsRead();
                    } else {
                        Timber.e(error);
                    }
                    mIsRefreshingMessages.postValue(false);
                }));
    }

    public void markNewDietAsSeen(Boolean hasBeenSeen) {
        mSharedPreferences.edit().putBoolean(Constants.HAS_NEW_UNREAD_DIET, !hasBeenSeen).apply();
    }

    public LiveData<Boolean> observeIfHasUnseenNewDiets() {
        return mDietRepository.getHasUnseenNewDiets();
    }
}




