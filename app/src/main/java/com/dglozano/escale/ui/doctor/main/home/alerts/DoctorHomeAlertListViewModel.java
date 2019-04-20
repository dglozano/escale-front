package com.dglozano.escale.ui.doctor.main.home.alerts;

import com.dglozano.escale.db.entity.Alert;
import com.dglozano.escale.repository.AlertRepository;
import com.dglozano.escale.repository.DoctorRepository;
import com.dglozano.escale.repository.PatientRepository;

import java.util.List;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class DoctorHomeAlertListViewModel extends ViewModel {

    private PatientRepository mPatientRepository;
    private DoctorRepository mDoctorRepository;
    private AlertRepository mAlertRepository;
    private CompositeDisposable disposables;
    private final LiveData<List<Alert>> alerts;

    @Inject
    public DoctorHomeAlertListViewModel(DoctorRepository doctorRepository,
                                        AlertRepository alertRepository,
                                        PatientRepository patientRepository) {
        mPatientRepository = patientRepository;
        mAlertRepository = alertRepository;
        mDoctorRepository = doctorRepository;
        disposables = new CompositeDisposable();
        alerts = alertRepository.getAllPatientAlertForDoctor(doctorRepository.getLoggedDoctorId(), patientRepository.getLoggedPatientId());
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }

    public LiveData<List<Alert>> getAlertsOfPatient() {
        return alerts;
    }

    public void toggleSeenByDoctor(Alert alert) {
        disposables.add(mAlertRepository.toggleSeenByDoctor(alert)
                .subscribeOn(Schedulers.io())
                .subscribe(() -> Timber.d("successfuly toggled seen by docter"), Timber::e));
    }
}