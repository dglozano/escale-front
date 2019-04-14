package com.dglozano.escale.ui.common.pw_recovery;

import com.dglozano.escale.R;
import com.dglozano.escale.util.ui.Event;
import com.dglozano.escale.web.EscaleRestApi;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class RecoverPasswordActivityViewModel extends ViewModel {

    private final CompositeDisposable disposables;
    private final MutableLiveData<Event<Integer>> mErrorEvent;
    private final MutableLiveData<Event<String>> mSuccessEvent;
    private final MutableLiveData<Boolean> mLoading;
    private EscaleRestApi mEscaleRestApi;

    @Inject
    public RecoverPasswordActivityViewModel(EscaleRestApi escaleRestApi) {
        mEscaleRestApi = escaleRestApi;
        mLoading = new MutableLiveData<>();
        mErrorEvent = new MutableLiveData<>();
        mSuccessEvent = new MutableLiveData<>();
        disposables = new CompositeDisposable();
        mLoading.postValue(false);
    }

    private boolean isInputValid(String email) {
        //TODO
        return true;
    }

    public LiveData<Boolean> getLoading() {
        return mLoading;
    }

    public LiveData<Event<Integer>> getErrorEvent() {
        return mErrorEvent;
    }

    public LiveData<Event<String>> getSuccessEvent() {
        return mSuccessEvent;
    }

    public void hitRecoverPassword(String email) {
        if(isInputValid(email)) {
            disposables.add(mEscaleRestApi.passwordRecovery(email)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe((d) -> mLoading.postValue(true))
                    .subscribe(
                            onSuccess -> {
                                mSuccessEvent.postValue(new Event<>(email));
                                mLoading.postValue(false);
                            },
                            onError -> {
                                mLoading.postValue(false);
                                mErrorEvent.postValue(new Event<>(R.string.recover_password_error_snackbar));
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
