package com.dglozano.escale.ui.main.home;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.repository.BodyMeasurementRepository;
import com.dglozano.escale.repository.PatientRepository;

import java.util.Optional;

import javax.inject.Inject;

public class HomeViewModel extends ViewModel {

    private LiveData<Patient> loggedUser;
    private PatientRepository mPatientRepository;
    private BodyMeasurementRepository mBodyMeasurementRepository;

    @Inject
    public HomeViewModel(PatientRepository patientRepository,
                         BodyMeasurementRepository bodyMeasurementRepository) {
        mPatientRepository = patientRepository;
        mBodyMeasurementRepository = bodyMeasurementRepository;

    }

    public LiveData<Optional<BodyMeasurement>> getLastBodyMeasurement() {
        return mBodyMeasurementRepository.getLastBodyMeasurementOfUserWithId(mPatientRepository.getLoggedPatiendId());
    }
}
