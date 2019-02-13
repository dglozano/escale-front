package com.dglozano.escale.ui.common;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.dglozano.escale.R;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.ui.Event;
import com.dglozano.escale.web.dto.ChangePasswordDataDTO;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ChangePasswordActivityViewModel extends ViewModel {

    private final CompositeDisposable disposables;
    private final MutableLiveData<Event<Integer>> mErrorEvent;
    private final MutableLiveData<Event<Long>> mSuccessEvent;
    private final MutableLiveData<Boolean> mLoading;
    private PatientRepository mPatientRepository;

    @Inject
    public ChangePasswordActivityViewModel(PatientRepository patientRepository) {
        mPatientRepository = patientRepository;
        mLoading = new MutableLiveData<>();
        mErrorEvent = new MutableLiveData<>();
        mSuccessEvent = new MutableLiveData<>();
        disposables = new CompositeDisposable();
        mLoading.postValue(false);
    }

    private boolean isInputValid(String currentPassword, String newPassword, String newPasswordRepeat) {
        //TODO
        return true;
    }

    public LiveData<Boolean> getLoading() {
        return mLoading;
    }

    public LiveData<Event<Integer>> getErrorEvent() {
        return mErrorEvent;
    }

    public LiveData<Event<Long>> getSuccessEvent() {
        return mSuccessEvent;
    }

    public void hitChangePassword(String currentPassword, String newPassword, String newPasswordRepeat) {
        if(isInputValid(currentPassword, newPassword, newPasswordRepeat)) {
            disposables.add(mPatientRepository.changePassword(currentPassword, newPassword, newPasswordRepeat)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe((d) -> mLoading.postValue(true))
                    .subscribe(
                            onSuccess -> mSuccessEvent.postValue(new Event<>(onSuccess)),
                            onError -> {
                                mLoading.postValue(false);
                                mErrorEvent.postValue(new Event<>(R.string.change_password_error_snackbar));
                            }
                    )
            );
        } else {
            // TODO
        }
    }


    @Override
    protected void onCleared() {
        disposables.clear();
    }
}
