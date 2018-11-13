package com.dglozano.escale.ui.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.dglozano.escale.db.entity.User;
import com.dglozano.escale.di.annotation.ApplicationScope;
import com.dglozano.escale.repository.UserRepository;

import java.util.List;

import javax.inject.Inject;

@ApplicationScope
public class MainActivityViewModel extends ViewModel {

    private LiveData<User> loggedUser;
    private UserRepository mUserRepository;

    @Inject
    public MainActivityViewModel(UserRepository userRepository) {
        mUserRepository = userRepository;
    }

    //FIXME no hace falta uno para todos los users
    public LiveData<List<User>> getAllUsers() {
        return mUserRepository.getAllUsers();
    }
}