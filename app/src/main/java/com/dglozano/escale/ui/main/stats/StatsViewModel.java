package com.dglozano.escale.ui.main.stats;

import com.dglozano.escale.repository.BodyMeasurementRepository;
import com.dglozano.escale.repository.PatientRepository;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import timber.log.Timber;

public class StatsViewModel extends ViewModel {

    private final LiveData<Boolean> mAreMeasurementsEmpty;
    private PatientRepository mPatientRepository;
    private BodyMeasurementRepository mMeasurementsRepository;

    @Inject
    public StatsViewModel(PatientRepository patientRepository,
                          BodyMeasurementRepository measurementRepository) {
        mPatientRepository = patientRepository;
        mMeasurementsRepository = measurementRepository;
        mAreMeasurementsEmpty = Transformations.map(
                mMeasurementsRepository.getLastBodyMeasurementOfUserWithId(
                        mPatientRepository.getLoggedPatientId()), measurement -> {
                    Timber.d("measurement %s", measurement.isPresent());
                    return !measurement.isPresent();
                });
    }

    public LiveData<Boolean> areMeasurementsEmpty() {
        return mAreMeasurementsEmpty;
    }
}
