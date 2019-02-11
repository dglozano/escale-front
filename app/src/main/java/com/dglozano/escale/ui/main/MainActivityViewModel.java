package com.dglozano.escale.ui.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.repository.PatientRepository;

import javax.inject.Inject;

public class MainActivityViewModel extends ViewModel {

    private PatientRepository mPatientRepository;
    private LiveData<Patient> mLoggedUser;

    public MainActivityViewModel(){}

    @Inject
    public MainActivityViewModel(PatientRepository patientRepository) {
        mPatientRepository = patientRepository;
    }

    public void initUserWithId(int userId) {
        //TODO
        mLoggedUser = mPatientRepository.getPatientById(userId);
    }

    public LiveData<Patient> getLoggedPatient() {
        return mLoggedUser;
    }
}