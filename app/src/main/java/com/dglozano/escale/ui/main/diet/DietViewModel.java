package com.dglozano.escale.ui.main.diet;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.db.entity.Diet;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.repository.BodyMeasurementRepository;
import com.dglozano.escale.repository.DietRepository;
import com.dglozano.escale.repository.PatientRepository;

import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

public class DietViewModel extends ViewModel {

    private PatientRepository mPatientRepository;
    private DietRepository mDietRepository;
    private final LiveData<Boolean> mAreDietsEmpty;

    @Inject
    public DietViewModel(PatientRepository patientRepository,
                         DietRepository dietRepository) {
        mPatientRepository = patientRepository;
        mDietRepository = dietRepository;
        mAreDietsEmpty = Transformations.map(mDietRepository.getCurrentDiet(
                mPatientRepository.getLoggedPatiendId()),
                Objects::isNull);
    }

    public LiveData<Boolean> areDietsEmpty() {
        return mAreDietsEmpty;
    }
}
