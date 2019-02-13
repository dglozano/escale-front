package com.dglozano.escale.ui.login;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.dglozano.escale.R;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.ui.Event;
import com.dglozano.escale.util.BadCredentialsException;
import com.dglozano.escale.util.NotAPatientException;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class LoginActivityViewModel extends ViewModel {

    public MutableLiveData<String> errorEmail;
    public MutableLiveData<String> errorPassword;
    private final CompositeDisposable disposables;
    private final MutableLiveData<Event<Integer>> mErrorEvent;
    private final MutableLiveData<Boolean> mLoading;
    private PatientRepository mPatientRepository;

    @Inject
    public LoginActivityViewModel(PatientRepository patientRepository) {
        errorEmail = new MutableLiveData<>();
        errorPassword = new MutableLiveData<>();
        mPatientRepository = patientRepository;
        disposables = new CompositeDisposable();
        mErrorEvent = new MutableLiveData<>();
        mLoading = new MutableLiveData<>();
        mLoading.postValue(false);
    }

    private boolean isInputValid(String email, String password) {
        //TODO
        return true;
    }

    public LiveData<Long> getLoggedUserId() {
        return mPatientRepository.getLoggedUserId();
    }

    public LiveData<Boolean> getLoading() {
        return mLoading;
    }

    public LiveData<Event<Integer>> getErrorEvent() {
        return mErrorEvent;
    }

    public void hitLogin(String email, String password) {

        if (isInputValid(email, password)) {
            disposables.add(mPatientRepository.loginPatient(email, password)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe((d) -> mLoading.postValue(true))
                    .subscribe(
                            () -> {}, // Do nothing on complete. After updating sharedPrefs it will trigger the rest
                            throwable -> {
                                Timber.e(throwable, "Error login ");
                                mLoading.postValue(false);
                                if(throwable instanceof BadCredentialsException) {
                                    mErrorEvent.postValue(new Event<>(R.string.login_error_bad_credentials));
                                } else if (throwable instanceof NotAPatientException) {
                                    mErrorEvent.postValue(new Event<>(R.string.login_error_not_patient));
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
}