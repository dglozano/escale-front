package com.dglozano.escale.ui.main;

import android.content.SharedPreferences;

import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.exception.AccountDisabledException;
import com.dglozano.escale.repository.BodyMeasurementRepository;
import com.dglozano.escale.repository.ChatRepository;
import com.dglozano.escale.repository.DietRepository;
import com.dglozano.escale.repository.DoctorRepository;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.repository.UserRepository;
import com.dglozano.escale.util.AbsentLiveData;
import com.dglozano.escale.util.Constants;
import com.dglozano.escale.util.ui.Event;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.NoSuchElementException;
import java.util.Objects;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class MainActivityViewModel extends ViewModel {

    private final LiveData<Patient> mLoggedPatient;
    private final MutableLiveData<Integer> positionOfCurrentFragment;
    private final SharedPreferences mSharedPreferences;
    private final CompositeDisposable disposables;
    private final MutableLiveData<Event<Integer>> mErrorEvent;
    private final MutableLiveData<Event<Integer>> mLogoutEvent;
    private PatientRepository mPatientRepository;
    private ChatRepository mChatRepository;
    private DoctorRepository mDoctorRepository;
    private DietRepository mDietRepository;
    private UserRepository mUserRepository;
    private BodyMeasurementRepository mMeasurementRepository;
    private LiveData<Event<Boolean>> mMustChangePassword;
    private MediatorLiveData<Boolean> mShowAppBarShadow;
    private LiveData<Boolean> mAreDietsEmpty;
    private LiveData<Boolean> mAreMeasurementsEmpty;
    private LiveData<Integer> mUnreadMessagesByPatient;
    private LiveData<Integer> mUnreadMessagesByDoctor;
    private MediatorLiveData<Boolean> mIsRefreshing;
    private MutableLiveData<Boolean> mIsRefreshingDiets;
    private MutableLiveData<Boolean> mIsRefreshingMessages;
    private MutableLiveData<Boolean> mIsRefreshingPatient;
    private MutableLiveData<Boolean> mIsRefreshingMeasurements;
    private boolean isDoctorView;

    @Inject
    public MainActivityViewModel(PatientRepository patientRepository,
                                 DoctorRepository doctorRepository,
                                 UserRepository userRepository,
                                 BodyMeasurementRepository bodyMeasurementRepository,
                                 SharedPreferences sharedPreferences,
                                 ChatRepository chatRepository, DietRepository dietRepository) {
        disposables = new CompositeDisposable();
        mUserRepository = userRepository;
        mDietRepository = dietRepository;
        mDoctorRepository = doctorRepository;
        mPatientRepository = patientRepository;
        mChatRepository = chatRepository;
        mMeasurementRepository = bodyMeasurementRepository;
        mSharedPreferences = sharedPreferences;
        mErrorEvent = new MutableLiveData<>();
        mLogoutEvent = new MutableLiveData<>();
        positionOfCurrentFragment = new MutableLiveData<>();
        positionOfCurrentFragment.setValue(0);
        mLoggedPatient = mPatientRepository.getLoggedPatient();
        mIsRefreshing = new MediatorLiveData<>();
        setupRefreshingObservable();

        mUnreadMessagesByPatient = Transformations.switchMap(
                mChatRepository.getAllChatsOfUser(mPatientRepository.getLoggedPatientId()),
                chats -> chats.isEmpty() ?
                        AbsentLiveData.create() :
                        mChatRepository.getUnreadMessagesOfUserInChatAsLiveData(mPatientRepository.getLoggedPatientId(),
                                chats.get(0).getId()));

        mUnreadMessagesByDoctor = Transformations.switchMap(
                mChatRepository.getAllChatsOfUser(mPatientRepository.getLoggedPatientId()),
                chats -> {
                    Timber.d("Chatis is empty %s", chats.isEmpty());
                    return chats.isEmpty() ?
                            AbsentLiveData.create() :
                            mChatRepository.getUnreadMessagesOfUserInChatAsLiveData(mDoctorRepository.getLoggedDoctorId(),
                                    chats.get(0).getId());
                });

        setupMustChangePasswordObservable();
        isDoctorView = false;

        setupAppBarShadowStatus();
    }


    public LiveData<Integer> getNumberOfUnreadMessagesByDoctor() {
        return mUnreadMessagesByDoctor;
    }

    public boolean isDoctorView() {
        return isDoctorView;
    }

    public void setDoctorView(boolean doctorView) {
        isDoctorView = doctorView;
    }

    private void setupAppBarShadowStatus() {
        mShowAppBarShadow = new MediatorLiveData<>();
        mShowAppBarShadow.setValue(false);
        mAreDietsEmpty = Transformations.map(mDietRepository.getCurrentDiet(
                mPatientRepository.getLoggedPatientId()),
                Objects::isNull);
        mAreMeasurementsEmpty = Transformations.map(
                mMeasurementRepository.getLastBodyMeasurementOfUserWithId(
                        mPatientRepository.getLoggedPatientId()), measurement -> !measurement.isPresent());
        mShowAppBarShadow.addSource(mAreDietsEmpty, dietsEmpty -> {
            checkEmptyStateAndFragmentPosition();
        });
        mShowAppBarShadow.addSource(mAreMeasurementsEmpty, measurementEmpty -> {
            checkEmptyStateAndFragmentPosition();
        });
        mShowAppBarShadow.addSource(positionOfCurrentFragment, positionFragment -> {
            checkEmptyStateAndFragmentPosition();
        });
        mShowAppBarShadow.addSource(mIsRefreshing, isLoading -> {
            if (!isLoading) {
                checkEmptyStateAndFragmentPosition();
            }
        });
    }

    private void checkEmptyStateAndFragmentPosition() {
        boolean dietsNotEmpty = mAreDietsEmpty.getValue() != null && !mAreDietsEmpty.getValue();
        boolean measurementsNotEmpty = mAreMeasurementsEmpty.getValue() != null
                && !mAreMeasurementsEmpty.getValue();
        boolean fragmentPositionDiets = positionOfCurrentFragment.getValue() != null
                && positionOfCurrentFragment.getValue() == 2;
        boolean fragmentPositionMeasurement = positionOfCurrentFragment.getValue() != null
                && positionOfCurrentFragment.getValue() == 1;
        boolean fragmentPositionHome = positionOfCurrentFragment.getValue() != null
                && positionOfCurrentFragment.getValue() == 0;
        if (dietsNotEmpty && fragmentPositionDiets) {
            mShowAppBarShadow.postValue(false);
        } else if (measurementsNotEmpty && fragmentPositionMeasurement) {
            mShowAppBarShadow.postValue(false);
        } else if (isDoctorView && fragmentPositionHome) {
            mShowAppBarShadow.postValue(false);
        } else {
            mShowAppBarShadow.postValue(true);
        }
    }

    public void refreshData() {
        disposables.add(mPatientRepository.refreshPatient(mPatientRepository.getLoggedPatientId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(d -> mIsRefreshingPatient.postValue(true))
                .subscribe(this::refreshEverythingElse,
                        error -> {
                            if (error instanceof AccountDisabledException
                                    || (error instanceof HttpException && error.getMessage().contains("401"))) {
                                Timber.d("The user's account was disabled or token is not valid");
                                mIsRefreshingPatient.postValue(false);
                                logout();
                            } else if (error instanceof NoSuchElementException) {
                                // Means that the user was fresh enough, so didn't have to call api
                                refreshEverythingElse(mPatientRepository.getLoggedPatientId());
                            } else {
                                mIsRefreshingPatient.postValue(false);
                                Timber.e(error);
                            }
                        })

        );
    }

    private void refreshEverythingElse(Long loggedPatientId) {
        if (mIsRefreshingPatient.getValue() != null && mIsRefreshingPatient.getValue()) {
            mIsRefreshingDiets.postValue(true);
            mIsRefreshingMessages.postValue(true);
            mIsRefreshingMeasurements.postValue(true);
            mIsRefreshingPatient.postValue(false);
            mSharedPreferences.edit()
                    .putLong(Constants.LAST_FULL_SYNC, Calendar.getInstance().getTimeInMillis())
                    .apply();
        }

        //TODO Use zip and only one livedata to represent loading status
        refreshMessages(loggedPatientId);
        refreshDiets(loggedPatientId);
        refreshMeasurements(loggedPatientId);
    }

    private void refreshMessages(Long patientId) {
        disposables.add(mChatRepository.refreshMessagesOfPatientWithId(patientId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    mIsRefreshingMessages.postValue(false);
                    Timber.d("Finished refreshing messages");
                }, error -> {
                    if (error instanceof NoSuchElementException) {
                        markMessagesAsReadForPatient();
                    } else {
                        Timber.e(error);
                    }
                    mIsRefreshingMessages.postValue(false);
                }));
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
                            Timber.d("Finished refreshing diets");
                        },
                        error -> {
                            Timber.e(error, "Error refreshing diets");
                            mIsRefreshingDiets.postValue(false);
                        })
        );
    }

    private void refreshMeasurements(Long patientId) {
        disposables.add(mMeasurementRepository.refreshMeasurements(patientId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                            mIsRefreshingMeasurements.postValue(false);
                            Timber.d("Finished refreshing measurements");
                        },
                        error -> {
                            Timber.e(error, "Error refreshing measurements");
                            mIsRefreshingMeasurements.postValue(false);
                        })
        );
    }

    private void setupRefreshingObservable() {
        mIsRefreshingMessages = new MutableLiveData<>();
        mIsRefreshingPatient = new MutableLiveData<>();
        mIsRefreshingDiets = new MutableLiveData<>();
        mIsRefreshingMeasurements = new MutableLiveData<>();
        mIsRefreshingMessages.setValue(true);
        mIsRefreshingPatient.setValue(true);
        mIsRefreshingDiets.setValue(true);
        mIsRefreshingMeasurements.setValue(true);

        mIsRefreshing.addSource(mIsRefreshingDiets, isRefreshingDiets ->
                mIsRefreshing.postValue(isRefreshingDiets ||
                        mIsRefreshingMessages.getValue() ||
                        mIsRefreshingMeasurements.getValue() ||
                        mIsRefreshingPatient.getValue()));
        mIsRefreshing.addSource(mIsRefreshingMessages, isRefreshingMessages ->
                mIsRefreshing.postValue(isRefreshingMessages ||
                        mIsRefreshingDiets.getValue() ||
                        mIsRefreshingMeasurements.getValue() ||
                        mIsRefreshingPatient.getValue()));
        mIsRefreshing.addSource(mIsRefreshingPatient, isRefreshingPatient ->
                mIsRefreshing.postValue(isRefreshingPatient ||
                        mIsRefreshingDiets.getValue() ||
                        mIsRefreshingMeasurements.getValue() ||
                        mIsRefreshingMessages.getValue())
        );
        mIsRefreshing.addSource(mIsRefreshingMeasurements, isRefreshingMeasurements ->
                mIsRefreshing.postValue(isRefreshingMeasurements ||
                        mIsRefreshingDiets.getValue() ||
                        mIsRefreshingPatient.getValue() ||
                        mIsRefreshingMessages.getValue())
        );
    }

    public void logout() {
        logout(-1);
    }

    public void logout(Integer logoutMessageResourceId) {
        mLogoutEvent.postValue(new Event<>(logoutMessageResourceId));
        mUserRepository.logout();
    }

    private void setupMustChangePasswordObservable() {
        mMustChangePassword = Transformations.map(mLoggedPatient,
                patient -> new Event<>(patient != null && !patient.hasChangedDefaultPassword())
        );
    }

    public LiveData<Integer> getPositionOfCurrentFragment() {
        return positionOfCurrentFragment;
    }

    public void setPositionOfCurrentFragment(int positionOfCurrentFragment) {
        this.positionOfCurrentFragment.postValue(positionOfCurrentFragment);
    }

    public MediatorLiveData<Boolean> isRefreshing() {
        return mIsRefreshing;
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

    public LiveData<Event<Integer>> getErrorEvent() {
        return mErrorEvent;
    }

    public LiveData<Event<Integer>> getLogoutEvent() {
        return mLogoutEvent;
    }

    public LiveData<Integer> getNumberOfUnreadMessagesByPatient() {
        return mUnreadMessagesByPatient;
    }

    public LiveData<String> getFirebaseToken() {
        return mUserRepository.getFirebaseDeviceToken();
    }

    public Boolean isFirebaseTokenSent() {
        return mUserRepository.isFirebaseTokenSent();
    }

    public Long getLoggedPatientId() {
        return mPatientRepository.getLoggedPatientId();
    }

    public void markMessagesAsReadForPatient() {
        disposables.add(mChatRepository.markMessagesAsReadForPatient()
                .subscribeOn(Schedulers.io())
                .subscribe(() -> Timber.d("mark as seen success"), e -> {
                    if (e instanceof NoSuchElementException) {
                        Timber.d("No chat for user, so didn't mark messages as read");
                    } else {
                        Timber.e(e);
                    }
                }));
    }

    public void markMessagesAsReadForDoctor() {
        disposables.add(mChatRepository.markMessagesAsReadForDoctor()
                .subscribeOn(Schedulers.io())
                .subscribe(() -> Timber.d("mark as seen success"), e -> {
                    if (e instanceof NoSuchElementException) {
                        Timber.d("No chat for user, so didn't mark messages as read");
                    } else {
                        Timber.e(e);
                    }
                }));
    }

    public void markNewDietAsSeen(Boolean hasBeenSeen) {
        mSharedPreferences.edit().putBoolean(Constants.HAS_NEW_UNREAD_DIET_SHARED_PREF, !hasBeenSeen).apply();
    }

    public LiveData<Boolean> observeIfHasUnseenNewDiets() {
        return mDietRepository.getHasUnseenNewDiets();
    }

    public MediatorLiveData<Boolean> getAppBarShadowStatus() {
        return mShowAppBarShadow;
    }

    public URL getProfileImageUrlOfLoggedPatient() throws MalformedURLException {
        return mPatientRepository.getProfileImageUrlOfLoggedPatient();
    }

    public Long getLoggedDoctorId() {
        return mDoctorRepository.getLoggedDoctorId();
    }
}




