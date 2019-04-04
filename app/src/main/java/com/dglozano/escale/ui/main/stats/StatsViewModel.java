package com.dglozano.escale.ui.main.stats;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import com.dglozano.escale.repository.BodyMeasurementRepository;
import com.dglozano.escale.repository.PatientRepository;

import java.util.Objects;

import javax.inject.Inject;

import timber.log.Timber;

public class StatsViewModel extends ViewModel {

    private PatientRepository mPatientRepository;
    private BodyMeasurementRepository mMeasurementsRepository;
    private final LiveData<Boolean> mAreMeasurementsEmpty;

    @Inject
    public StatsViewModel(PatientRepository patientRepository,
                          BodyMeasurementRepository measurementRepository) {
        mPatientRepository = patientRepository;
        mMeasurementsRepository = measurementRepository;
        mAreMeasurementsEmpty = Transformations.map(
                mMeasurementsRepository.getLastBodyMeasurementOfUserWithId(
                        mPatientRepository.getLoggedPatiendId()), measurement -> {
                    Timber.d("measurement %s", measurement.isPresent());
                    return !measurement.isPresent();
                });
    }

    public LiveData<Boolean> areMeasurementsEmpty() {
        return mAreMeasurementsEmpty;
    }
}
