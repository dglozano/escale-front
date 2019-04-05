package com.dglozano.escale.ui.main.diet;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.dglozano.escale.repository.DietRepository;
import com.dglozano.escale.repository.PatientRepository;

import java.util.Objects;

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
