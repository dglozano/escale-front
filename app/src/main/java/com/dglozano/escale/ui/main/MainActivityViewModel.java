package com.dglozano.escale.ui.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.ui.Event;

import java.util.Objects;

import javax.inject.Inject;

public class MainActivityViewModel extends ViewModel {

    private PatientRepository mPatientRepository;
    private final LiveData<Event<Boolean>> mMustChangePassword;
    private final LiveData<Patient> mLoggedPatient;

    @Inject
    public MainActivityViewModel(PatientRepository patientRepository) {
        mPatientRepository = patientRepository;
        mLoggedPatient = mPatientRepository.getLoggedPatient();
        mMustChangePassword = Transformations.map(mLoggedPatient,
                patient -> new Event<>(patient != null && !patient.hasChangedDefaultPassword())
        );
    }

    public LiveData<Patient> getLoggedPatient() {
        return mLoggedPatient;
    }

    public LiveData<Event<Boolean>> watchMustChangePassword() {
        return mMustChangePassword;
    }

    public void handleMustChangePasswordEvent() {
        Objects.requireNonNull(mMustChangePassword.getValue()).handleContent();
    }

    public void logout() {
        mPatientRepository.logout();
    }
}