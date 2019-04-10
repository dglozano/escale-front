package com.dglozano.escale.ui.main.home;

import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.repository.BodyMeasurementRepository;
import com.dglozano.escale.repository.PatientRepository;

import java.util.Date;
import java.util.Optional;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private PatientRepository mPatientRepository;
    private BodyMeasurementRepository mBodyMeasurementRepository;
    private boolean isScaleConnected = false;
    private int batteryLevel = -1;
    private LiveData<Optional<BodyMeasurement>> lastMeasurementOfLoggedUser;


    @Inject
    public HomeViewModel(PatientRepository patientRepository,
                         BodyMeasurementRepository bodyMeasurementRepository) {
        mPatientRepository = patientRepository;
        mBodyMeasurementRepository = bodyMeasurementRepository;
        lastMeasurementOfLoggedUser = mBodyMeasurementRepository.getLastBodyMeasurementOfUserWithId(mPatientRepository.getLoggedPatiendId());
    }

    public LiveData<Optional<BodyMeasurement>> getLastBodyMeasurement() {
        return lastMeasurementOfLoggedUser;
    }

    public LiveData<Patient> getLoggedPatient() {
        return mPatientRepository.getLoggedPatient();
    }

    public boolean isScaleConnected() {
        return isScaleConnected;
    }

    public boolean setIsScaleConnected(boolean flag) {
        return isScaleConnected = flag;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public Optional<Float> getGoalOfLoggedPatient() {
        return mPatientRepository.getGoalOfPatientWithId(mPatientRepository.getLoggedPatiendId());
    }

    public Optional<BodyMeasurement> getLastBodyMeasurementBeforeGoalStarted(Date goalStartDate, Long patientId) {
        return mBodyMeasurementRepository.getLastBodyMeasurementBeforeGoalStarted(goalStartDate, patientId);
    }

    public Optional<BodyMeasurement> getFirstBodyMeasurementAfterGoalStarted(Date goalStartDate, Long patientId) {
        return mBodyMeasurementRepository.getFirstBodyMeasurementAfterGoalStarted(goalStartDate, patientId);
    }

    public Optional<BodyMeasurement> getLastBodyMeasurementBlocking() {
        return mBodyMeasurementRepository.getLastBodyMeasurementBlockingOfPatient(mPatientRepository.getLoggedPatiendId());
    }
}
