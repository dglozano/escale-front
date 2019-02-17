package com.dglozano.escale.ui.main.diet;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.dglozano.escale.db.entity.Diet;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.repository.BodyMeasurementRepository;
import com.dglozano.escale.repository.DietRepository;
import com.dglozano.escale.repository.PatientRepository;

import java.util.List;

import javax.inject.Inject;

public class DietViewModel extends ViewModel {

    private PatientRepository mPatientRepository;
    private DietRepository mDietRepository;
    private LiveData<List<Diet>> mPatientDiets;

    @Inject
    public DietViewModel(PatientRepository patientRepository,
                         DietRepository dietRepository
                         ) {
        mPatientRepository = patientRepository;
        mDietRepository = dietRepository;
        mPatientDiets = mDietRepository.getDietsOfPatientWithId(mPatientRepository.getLoggedPatiendId());
    }

    public LiveData<List<Diet>> getDietsOfLoggedPatient() {
        return mPatientDiets;
    }
}
