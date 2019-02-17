package com.dglozano.escale.repository;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;

import com.dglozano.escale.db.dao.DietDao;
import com.dglozano.escale.db.dao.PatientDao;
import com.dglozano.escale.db.entity.Diet;
import com.dglozano.escale.di.annotation.ApplicationScope;
import com.dglozano.escale.web.EscaleRestApi;
import com.dglozano.escale.web.dto.DietDTO;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@ApplicationScope
public class DietRepository {

    private PatientDao mPatientDao;
    private DietDao mDietDao;
    private EscaleRestApi mEscaleRestApi;

    @Inject
    public DietRepository(EscaleRestApi escaleRestApi, PatientDao patientDao,
                          DietDao dietDao) {
        mPatientDao = patientDao;
        mEscaleRestApi = escaleRestApi;
        mDietDao = dietDao;
    }

    public LiveData<List<Diet>> getDietsOfPatientWithId(Long patientId) {
        refreshDiets(patientId);
        return mDietDao.getAllDietsOfUserWithIdAsLiveData(patientId);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    private void refreshDiets(final Long patientId) {
        refreshDietsCompletable(patientId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(() -> Timber.d("Success refresh diets"), e -> {
                        Timber.e(e, "Failed refresh diets");
                });
    }

    public Completable refreshDietsCompletable(Long patientId) {
        return mEscaleRestApi.getDiets(patientId)
                .flatMapCompletable(dietsApi -> {
                    Timber.d("Retrieved diets for user with id %s from Api", patientId);
                    dietsApi.stream()
                            .filter(dietDTO -> mDietDao.dietExists(dietDTO.getUuid()) != 1)
                            .map(dietDTO -> new Diet(dietDTO, patientId))
                            .forEach(diet -> {
                                Timber.d("Inserting diet from API %s", diet.getId());
                                mDietDao.insertDiet(diet);
                            });

                    Set<String> dietUuids =
                            dietsApi.stream()
                                    .map(DietDTO::getUuid)
                                    .collect(Collectors.toSet());

                    List<Diet> dietsInDatabase= mDietDao.getAllDietsOfUserWithId(patientId);


                    if(dietsInDatabase != null) {
                        dietsInDatabase
                                .stream()
                                .filter(diet -> !dietUuids.contains(diet.getId()))
                                .forEach(diet -> mDietDao.deleteDiet(diet));
                    }

                    return Completable.complete();
                });
    }
}
