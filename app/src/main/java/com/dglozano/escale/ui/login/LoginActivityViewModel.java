package com.dglozano.escale.ui.login;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.dglozano.escale.web.dto.Credentials;

import javax.inject.Inject;

public class LoginActivityViewModel extends ViewModel {

    public MutableLiveData<String> email;
    public MutableLiveData<String> errorEmail;
    public MutableLiveData<String> password;
    public MutableLiveData<String> errorPassword;

    @Inject
    public LoginActivityViewModel() {
        email = new MutableLiveData<>();
        errorEmail = new MutableLiveData<>();
        password = new MutableLiveData<>();
        errorPassword = new MutableLiveData<>();
    }

    public Credentials getCredentials() {
        if(isValid()) {
            return new Credentials(email.getValue(), password.getValue());
        }
        return null;
        //TODO
    }

    private boolean isValid() {
        //TODO
        return true;
    }
}
