package com.dglozano.escale.ui.doctor.main.home.profile;

import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.repository.BodyMeasurementRepository;
import com.dglozano.escale.repository.PatientRepository;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Optional;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class DoctorHomeProfileViewModel extends ViewModel {

    private PatientRepository mPatientRepository;
    private BodyMeasurementRepository mBodyMeasurementRepository;

    private LiveData<Optional<BodyMeasurement>> lastMeasurementOfLoggedUser;
    private LiveData<Patient> mLoggedPatient;


    @Inject
    public DoctorHomeProfileViewModel(PatientRepository patientRepository,
                                      BodyMeasurementRepository bodyMeasurementRepository) {
        mPatientRepository = patientRepository;
        mBodyMeasurementRepository = bodyMeasurementRepository;
        mLoggedPatient = mPatientRepository.getLoggedPatient();
        lastMeasurementOfLoggedUser = mBodyMeasurementRepository.getLastBodyMeasurementOfUserWithId(mPatientRepository.getLoggedPatientId());
    }


    public URL getProfileImageUrlOfLoggedPatient() throws MalformedURLException {
        return mPatientRepository.getProfileImageUrlOfLoggedPatient();
    }

    public LiveData<Patient> getLoggedPatient() {
        return mLoggedPatient;
    }

    public LiveData<Optional<BodyMeasurement>> getLastBodyMeasurement() {
        return lastMeasurementOfLoggedUser;
    }

    public Optional<Float> getGoalOfLoggedPatient() {
        return mPatientRepository.getGoalOfPatientWithId(mPatientRepository.getLoggedPatientId());
    }

    public Optional<BodyMeasurement> getLastBodyMeasurementBeforeGoalStarted(Date goalStartDate, Long patientId) {
        return mBodyMeasurementRepository.getLastBodyMeasurementBeforeGoalStarted(goalStartDate, patientId);
    }

    public Optional<BodyMeasurement> getFirstBodyMeasurementAfterGoalStarted(Date goalStartDate, Long patientId) {
        return mBodyMeasurementRepository.getFirstBodyMeasurementAfterGoalStarted(goalStartDate, patientId);
    }

    public Optional<BodyMeasurement> getLastBodyMeasurementBlocking() {
        return mBodyMeasurementRepository.getLastBodyMeasurementBlockingOfPatient(mPatientRepository.getLoggedPatientId());
    }
}
