package com.dglozano.escale.ui.main.stats.list;

import android.arch.lifecycle.ViewModel;

import com.dglozano.escale.repository.BodyMeasurementRepository;
import com.dglozano.escale.repository.PatientRepository;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class StatsListViewModel extends ViewModel {

    private PatientRepository mPatientRepository;
    private BodyMeasurementRepository mMeasurementsRepository;
    private CompositeDisposable disposables;

    @Inject
    public StatsListViewModel(BodyMeasurementRepository bodyMeasurementRepository,
                              PatientRepository patientRepository) {
        mPatientRepository = patientRepository;
        mMeasurementsRepository = bodyMeasurementRepository;
        disposables = new CompositeDisposable();

    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }
}