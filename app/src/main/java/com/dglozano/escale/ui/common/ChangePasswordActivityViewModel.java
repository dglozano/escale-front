package com.dglozano.escale.ui.common;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.dglozano.escale.web.dto.ChangePasswordDataDTO;

import javax.inject.Inject;

public class ChangePasswordActivityViewModel extends ViewModel {

    public MutableLiveData<String> currentPassword;
    public MutableLiveData<String> newPassword;
    public MutableLiveData<String> errorNewPassword;
    public MutableLiveData<String> newPasswordRepeat;
    public MutableLiveData<String> errorNewPasswordRepeat;
    private int userId;

    @Inject
    public ChangePasswordActivityViewModel() {
        currentPassword = new MutableLiveData<>();
        newPassword = new MutableLiveData<>();
        errorNewPassword = new MutableLiveData<>();
        newPasswordRepeat = new MutableLiveData<>();
        errorNewPasswordRepeat = new MutableLiveData<>();
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public ChangePasswordDataDTO getChangePasswordData() {
        if(isValid()) {
            return new ChangePasswordDataDTO(currentPassword.getValue(), newPassword.getValue(), newPasswordRepeat.getValue());
        }
        return null;
        //TODO
    }

    private boolean isValid() {
        //TODO
        return true;
    }
}
