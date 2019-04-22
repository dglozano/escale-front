package com.dglozano.escale.ui.doctor.main.home;

import com.dglozano.escale.repository.AlertRepository;
import com.dglozano.escale.repository.DoctorRepository;
import com.dglozano.escale.repository.PatientRepository;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class DoctorHomeViewModel extends ViewModel {

    private final PatientRepository mPatientRepository;
    private final DoctorRepository mDoctorRepository;
    private final AlertRepository mAlertRepository;
    private final CompositeDisposable disposables;

    @Inject
    public DoctorHomeViewModel(PatientRepository patientRepository,
                               AlertRepository alertRepository,
                               DoctorRepository doctorRepository) {
        mPatientRepository = patientRepository;
        mAlertRepository = alertRepository;
        mDoctorRepository = doctorRepository;
        disposables = new CompositeDisposable();

    }

    public LiveData<Integer> getUnseenAlertsForDoctor() {
        return mAlertRepository.getCountOfPatientAlertsNotSeenByDoctor(mDoctorRepository.getLoggedDoctorId(), mPatientRepository.getLoggedPatientId());
    }

    public void markAllAsSeen() {
        disposables.add(mAlertRepository.markAllAsSeen(mDoctorRepository.getLoggedDoctorId(), mPatientRepository.getLoggedPatientId())
                .subscribeOn(Schedulers.io())
                .subscribe(() -> Timber.d("successfuly toggled seen by docter"), Timber::e));
    }
}
