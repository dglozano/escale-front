package com.dglozano.escale.ui.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.dglozano.escale.db.entity.User;
import com.dglozano.escale.repository.UserRepository;

import javax.inject.Inject;

public class MainActivityViewModel extends ViewModel {

    private UserRepository mUserRepository;
    private LiveData<User> mLoggedUser;

    @Inject
    public MainActivityViewModel(UserRepository userRepository) {
        mUserRepository = userRepository;
    }

    public void initUserWithId(int userId) {
        //TODO
        mLoggedUser = mUserRepository.getUserById(userId);
    }

    public LiveData<User> getLoggedUser() {
        return mLoggedUser;
    }
}