package com.dglozano.escale.ui.login;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import javax.inject.Inject;

public class LoginActivityViewModel extends ViewModel {

    public MutableLiveData<String> email;
    public MutableLiveData<String> errorEmail;
    public MutableLiveData<String> password;
    public MutableLiveData<String> errorPassword;

    @Inject
    public LoginActivityViewModel() {
    }
}
