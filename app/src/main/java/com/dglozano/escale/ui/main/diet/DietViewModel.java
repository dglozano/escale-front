package com.dglozano.escale.ui.main.diet;

import com.dglozano.escale.repository.DietRepository;
import com.dglozano.escale.repository.PatientRepository;

import java.util.Objects;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

public class DietViewModel extends ViewModel {

    private final PatientRepository mPatientRepository;
    private final DietRepository mDietRepository;
    private final LiveData<Boolean> mAreDietsEmpty;

    @Inject
    public DietViewModel(PatientRepository patientRepository,
                         DietRepository dietRepository) {
        mPatientRepository = patientRepository;
        mDietRepository = dietRepository;

        mAreDietsEmpty = Transformations.map(mDietRepository.getCurrentDiet(
                mPatientRepository.getLoggedPatientId()),
                Objects::isNull);
    }

    public LiveData<Boolean> areDietsEmpty() {
        return mAreDietsEmpty;
    }
}
