package com.dglozano.escale.ui.main.stats.list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.repository.BodyMeasurementRepository;
import com.dglozano.escale.repository.PatientRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class StatsListViewModel extends ViewModel {

    private PatientRepository mPatientRepository;
    private BodyMeasurementRepository mMeasurementsRepository;
    private CompositeDisposable disposables;
    private LiveData<List<BodyMeasurement>> mBodyMeasurementList;


    @Inject
    public StatsListViewModel(BodyMeasurementRepository bodyMeasurementRepository,
                              PatientRepository patientRepository) {
        mPatientRepository = patientRepository;
        mMeasurementsRepository = bodyMeasurementRepository;
        disposables = new CompositeDisposable();
        mBodyMeasurementList = mMeasurementsRepository.getLastBodyMeasurementsOfUserWithId(
                mPatientRepository.getLoggedPatiendId());
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }

    public LiveData<List<BodyMeasurement>> getStatsOfLoggedUser() {
        return mBodyMeasurementList;
    }
}