package com.dglozano.escale.ui.common.pw_change;

import com.dglozano.escale.R;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.repository.UserRepository;
import com.dglozano.escale.util.ui.Event;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.dglozano.escale.util.ValidationHelper.isValidPassword;

public class ChangePasswordActivityViewModel extends ViewModel {

    private final CompositeDisposable disposables;
    private final MutableLiveData<Event<Integer>> mErrorEvent;
    private final MutableLiveData<Event<Long>> mSuccessEvent;
    private final MutableLiveData<Boolean> mLoading;
    private UserRepository mUserRepository;
    private Long userId;

    @Inject
    public ChangePasswordActivityViewModel(UserRepository userRepository) {
        mUserRepository = userRepository;
        mLoading = new MutableLiveData<>();
        mErrorEvent = new MutableLiveData<>();
        mSuccessEvent = new MutableLiveData<>();
        disposables = new CompositeDisposable();
        mLoading.postValue(false);
    }

    private boolean isInputValid(CharSequence currentPassword,
                                 CharSequence newPassword,
                                 CharSequence newPasswordRepeat) {
        return isValidPassword(currentPassword)
                && isValidPassword(newPassword)
                && isValidPassword(newPasswordRepeat)
                && newPasswordRepeat.equals(newPassword)
                && !newPassword.equals(currentPassword);
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public void hitChangePassword(CharSequence currentPassword, CharSequence newPassword, CharSequence newPasswordRepeat) {
        if(isInputValid(currentPassword, newPassword, newPasswordRepeat)) {
            disposables.add(mUserRepository.changePassword(userId, currentPassword.toString(), newPassword.toString(), newPasswordRepeat.toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe((d) -> mLoading.postValue(true))
                    .subscribe(
                            onSuccess -> {
                                mSuccessEvent.postValue(new Event<>(onSuccess));
                                mLoading.postValue(false);
                            },
                            onError -> {
                                mLoading.postValue(false);
                                mErrorEvent.postValue(new Event<>(R.string.change_password_error_snackbar));
                            }
                    )
            );
        } else {
            mErrorEvent.postValue(new Event<>(R.string.input_validation_error_snackbar));
        }
    }


    @Override
    protected void onCleared() {
        disposables.clear();
    }
}
