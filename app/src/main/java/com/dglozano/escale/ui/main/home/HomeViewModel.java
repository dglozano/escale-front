package com.dglozano.escale.ui.main.home;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.dglozano.escale.db.entity.User;
import com.dglozano.escale.repository.BodyMeasurementRepository;
import com.dglozano.escale.repository.UserRepository;

import javax.inject.Inject;

public class HomeViewModel extends ViewModel {

    private LiveData<User> loggedUser;
    private UserRepository mUserRepository;
    private BodyMeasurementRepository mBodyMeasurementRepository;

    @Inject
    public HomeViewModel(UserRepository userRepository,
                         BodyMeasurementRepository bodyMeasurementRepository) {
        mUserRepository = userRepository;
        mBodyMeasurementRepository = bodyMeasurementRepository;
    }
}
