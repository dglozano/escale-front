package com.dglozano.escale.ui.login;

import com.dglozano.escale.R;
import com.dglozano.escale.exception.AccountDisabledException;
import com.dglozano.escale.exception.BadCredentialsException;
import com.dglozano.escale.exception.NotMobileAppUser;
import com.dglozano.escale.repository.DoctorRepository;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.repository.UserRepository;
import com.dglozano.escale.util.ui.Event;
import com.google.firebase.iid.FirebaseInstanceId;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class LoginActivityViewModel extends ViewModel {

    private final CompositeDisposable disposables;
    private final MutableLiveData<Event<Integer>> mErrorEvent;
    private final LiveData<Event<Long>> mPatientIdChanged;
    private final LiveData<Event<Long>> mDoctorIdChanged;
    private final MutableLiveData<Boolean> mLoading;
    public MutableLiveData<String> errorEmail;
    public MutableLiveData<String> errorPassword;
    private PatientRepository mPatientRepository;
    private DoctorRepository mDoctorRepository;
    private UserRepository mUserRepository;

    @Inject
    public LoginActivityViewModel(PatientRepository patientRepository,
                                  DoctorRepository doctorRepository,
                                  UserRepository userRepository) {
        mPatientRepository = patientRepository;
        mDoctorRepository = doctorRepository;
        mUserRepository = userRepository;

        errorEmail = new MutableLiveData<>();
        errorPassword = new MutableLiveData<>();
        disposables = new CompositeDisposable();
        mErrorEvent = new MutableLiveData<>();
        mLoading = new MutableLiveData<>();

        mPatientIdChanged = Transformations.map(mPatientRepository.getLoggedPatientIdAsLiveData(), Event::new);
        mDoctorIdChanged = Transformations.map(mDoctorRepository.getLoggedDoctorIdAsLiveData(), Event::new);

        mLoading.postValue(false);
    }

    private boolean isInputValid(String email, String password) {
        //TODO
        return true;
    }

    public LiveData<Event<Long>> getPatientIdChangedEvent() {
        return mPatientIdChanged;
    }

    public LiveData<Boolean> getLoading() {
        return mLoading;
    }

    public LiveData<Event<Integer>> getErrorEvent() {
        return mErrorEvent;
    }

    public void hitLogin(String email, String password) {

        if (isInputValid(email, password)) {
            disposables.add(mUserRepository.loginUser(email, password)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe((d) -> mLoading.postValue(true))
                    .subscribe(
                            () -> {
                            }, // Do nothing on complete. After updating sharedPrefs it will trigger the rest
                            throwable -> {
                                mLoading.postValue(false);
                                if (throwable instanceof BadCredentialsException) {
                                    mErrorEvent.postValue(new Event<>(R.string.login_error_bad_credentials));
                                } else if (throwable instanceof NotMobileAppUser) {
                                    mErrorEvent.postValue(new Event<>(R.string.login_error_not_mobile_user));
                                } else if (throwable instanceof AccountDisabledException) {
                                    mErrorEvent.postValue(new Event<>(R.string.account_disabled_error_msg));
                                } else {
                                    mErrorEvent.postValue(new Event<>(R.string.login_error_could_not_reach_server));
                                }
                            }
                    ));
        } else {
            //TODO error show
        }
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }


    //TODO Show loader and wait until finish to be able to login again
    public void askForNewFirebaseToken() {
        disposables.add(Completable.fromCallable(() -> {
            Timber.d("Deleting firebase token");
            FirebaseInstanceId.getInstance().deleteInstanceId();
            return Completable.complete();
        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(() -> Timber.d("Deleted firebase token succesfully"), Timber::e));
    }

    public LiveData<Event<Long>> getDoctorIdChangedEvent() {
        return mDoctorIdChanged;
    }

    public long getLoggedDoctorId() {
        return mDoctorRepository.getLoggedDoctorId();
    }
}
