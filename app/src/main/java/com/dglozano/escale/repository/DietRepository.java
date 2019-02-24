package com.dglozano.escale.repository;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;

import com.dglozano.escale.db.dao.DietDao;
import com.dglozano.escale.db.dao.PatientDao;
import com.dglozano.escale.db.entity.Diet;
import com.dglozano.escale.di.annotation.ApplicationScope;
import com.dglozano.escale.di.annotation.RootFileDirectory;
import com.dglozano.escale.exception.DietDownloadStateException;
import com.dglozano.escale.util.AppExecutors;
import com.dglozano.escale.util.FileUtil;
import com.dglozano.escale.web.EscaleRestApi;
import com.dglozano.escale.web.dto.DietDTO;

import java.io.File;
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
    private File mFileDirectory;
    private AppExecutors mAppExecutors;

    @Inject
    public DietRepository(EscaleRestApi escaleRestApi, PatientDao patientDao,
                          DietDao dietDao,@RootFileDirectory File fileDirectory, AppExecutors appExecutors) {
        mPatientDao = patientDao;
        mEscaleRestApi = escaleRestApi;
        mDietDao = dietDao;
        mFileDirectory = fileDirectory;
        mAppExecutors = appExecutors;
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

                    List<Diet> dietsInDatabase = mDietDao.getAllDietsOfUserWithId(patientId);


                    if (dietsInDatabase != null) {
                        dietsInDatabase
                                .stream()
                                .filter(diet -> !dietUuids.contains(diet.getId()))
                                .forEach(diet -> mDietDao.deleteDiet(diet));
                    }

                    return Completable.complete();
                });
    }

//    public Completable download(Diet diet) throws DietDownloadStateException {
//        if (diet.getFileStatus().equals(Diet.FileStatus.NOT_DOWNLOADED)) {
//            return mEscaleRestApi.downloadDiet(diet.getId())
//                    .flatMapCompletable(responseBody -> {
//                        Timber.d("Server contacted and has file");
//                        boolean writtenToDisk = FileUtil.writeResponseBodyToDisk(responseBody,
//                                mFileDirectory,
//                                diet.getLocalFileName());
//                        Timber.d("File download was a success? %s", writtenToDisk);
//                        if (writtenToDisk) {
//                            diet.setFileStatus(Diet.FileStatus.DOWNLOADED);
//                            mDietDao.updateDiet(diet);
//                        } else {
//                            diet.setFileStatus(Diet.FileStatus.NOT_DOWNLOADED);
//                            mDietDao.updateDiet(diet);
//                        }
//                        return Completable.complete();
//                    }).doOnSubscribe(disposable -> {
//                        Timber.d("Changing status to downloading");
//                        diet.setFileStatus(Diet.FileStatus.DOWNLOADING);
//                        mDietDao.updateDiet(diet);
//                    });
//        } else {
//            throw new DietDownloadStateException("La dieta ya esta descargada o descargandose.");
//        }
//    }

    public void deleteDownload(Diet diet) throws DietDownloadStateException {
        File file = getDietPdfFile(diet);
        mAppExecutors.getDiskIO().execute(() -> {
            boolean deleted = file.delete();
            Timber.e("Was file %s deleted? %s", file, deleted);
            diet.setFileStatus(Diet.FileStatus.NOT_DOWNLOADED);
            mDietDao.updateDiet(diet);
        });
    }

    public File getDietPdfFile(Diet diet) throws DietDownloadStateException {
        if (diet.getFileStatus().equals(Diet.FileStatus.DOWNLOADED)) {
            return new File(mFileDirectory.getPath(), diet.getLocalFileName());
        } else {
            throw new DietDownloadStateException("La dieta no se encuentra descargada.");
        }
    }

    public void updateDiet(Diet diet) {
        mAppExecutors.getDiskIO().execute(() -> mDietDao.updateDiet(diet));
    }

    public LiveData<Diet> getCurrentDiet(Long loggedPatiendId) {
        refreshDiets(loggedPatiendId);
        return mDietDao.getCurrenDietOfUserWithIdAsLiveData(loggedPatiendId);
    }
}
