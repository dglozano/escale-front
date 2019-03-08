package com.dglozano.escale.repository;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.content.SharedPreferences;

import com.dglozano.escale.db.dao.DietDao;
import com.dglozano.escale.db.entity.Diet;
import com.dglozano.escale.di.annotation.ApplicationScope;
import com.dglozano.escale.di.annotation.RootFileDirectory;
import com.dglozano.escale.exception.DietDownloadStateException;
import com.dglozano.escale.util.AppExecutors;
import com.dglozano.escale.util.Constants;
import com.dglozano.escale.util.SharedPreferencesLiveData;
import com.dglozano.escale.web.EscaleRestApi;
import com.dglozano.escale.web.dto.DietDTO;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@ApplicationScope
public class DietRepository {

    private DietDao mDietDao;
    private EscaleRestApi mEscaleRestApi;
    private File mFileDirectory;
    private AppExecutors mAppExecutors;
    private LiveData<Boolean> mHasNewUnseenDiets;
    private SharedPreferences mSharedPreferences;

    @Inject
    public DietRepository(EscaleRestApi escaleRestApi,
                          DietDao dietDao,
                          @RootFileDirectory File fileDirectory,
                          AppExecutors appExecutors,
                          SharedPreferences sharedPreferences) {
        this.mSharedPreferences = sharedPreferences;
        mEscaleRestApi = escaleRestApi;
        mDietDao = dietDao;
        mFileDirectory = fileDirectory;
        mAppExecutors = appExecutors;
        mHasNewUnseenDiets = new SharedPreferencesLiveData.SharedPreferenceBooleanLiveData(
                mSharedPreferences,
                Constants.HAS_NEW_UNREAD_DIET, false);
    }

    public LiveData<Boolean> getHasUnseenNewDiets() {
        return mHasNewUnseenDiets;
    }

    public LiveData<List<Diet>> getDietsOfPatientWithId(Long patientId) {
//        refreshDiets(patientId);
        return mDietDao.getAllDietsOfUserWithIdAsLiveData(patientId);
    }

    public LiveData<Diet> getCurrentDiet(Long loggedPatiendId) {
//        refreshDiets(loggedPatiendId);
        return mDietDao.getCurrenDietOfUserWithIdAsLiveData(loggedPatiendId);
    }


//    private void refreshDiets(final Long patientId) {
//        refreshDietsCompletable(patientId)
//                .subscribeOn(Schedulers.io())
//                .observeOn(Schedulers.io())
//                .subscribe(() -> Timber.d("Success refresh diets"), e -> {
//                    Timber.e(e, "Failed refresh diets");
//                });
//    }

    public Single<Integer> refreshDietsSingle(Long patientId) {
        return mEscaleRestApi.getDiets(patientId)
                .flatMap(dietsApi -> {
                    Timber.d("Retrieved diets for user with id %s from Api", patientId);
                    List<Diet> newDietsToAdd = dietsApi.stream()
                            .filter(dietDTO -> mDietDao.dietExists(dietDTO.getUuid()) != 1)
                            .map(dietDTO -> new Diet(dietDTO, patientId))
                            .collect(Collectors.toList());

                    newDietsToAdd.forEach(diet -> {
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

                    return Single.just(newDietsToAdd.size());
                });
    }

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

    public Completable saveDietOnNotified(Long patientId, String uuid, String fileName,
                                          String dateString, Long size) {
        return Completable.fromCallable(() -> {
            String format = "yyyy-MM-dd'T'HH:mm:ss.SSS";
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date date = sdf.parse(dateString);
            mDietDao.insertDiet(new Diet(patientId, uuid, fileName, date, size));
            return Completable.complete();
        });
    }

    public Completable deleteDietByUuid(String uuid) {
        return Completable.fromCallable(() -> {
            Optional<Diet> diet = mDietDao.getDietById(uuid);
            if(diet.isPresent()) {
                if (diet.get().getFileStatus().equals(Diet.FileStatus.DOWNLOADED)) {
                    boolean deleted = new File(mFileDirectory.getPath(), diet.get().getLocalFileName()).delete();
                    Timber.e("Was diet file deleted? %s", deleted);
                }
                mDietDao.deleteDiet(diet.get());
            }
            return Completable.complete();
        });
    }
}
