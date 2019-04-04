package com.dglozano.escale.web.services;

import android.content.Intent;
import android.support.annotation.Nullable;

import com.dglozano.escale.db.dao.DietDao;
import com.dglozano.escale.db.entity.Diet;
import com.dglozano.escale.util.FileUtils;

import java.util.Optional;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public class DietDownloadService extends AbstractDownloadService {

    @Inject
    DietDao dietDao;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Timber.d("Handling intent in DietDownloadService %s", intent);
        if (intent != null) {
            String dietUuid = intent.getStringExtra("diet-uuid");
            Optional<Diet> dietOptional = dietDao.getDietById(dietUuid);
            if (dietOptional.isPresent()) {
                Diet diet = dietOptional.get();
                Call<ResponseBody> call = escaleRestApi.downloadDiet(dietUuid);
                try {
                    Response<ResponseBody> response = call.execute();
                    if (!response.isSuccessful() || response.body() == null)
                        throw new Exception("Response was not successful");
                    boolean writtenToDisk = FileUtils.writeResponseBodyToDisk(response.body(),
                            mFileDirectory,
                            diet.getLocalFileName());
                    Timber.d("File download was a success? %s", writtenToDisk);
                    if (writtenToDisk) {
                        diet.setFileStatus(FileUtils.FileStatus.DOWNLOADED);
                        dietDao.updateDiet(diet);
                    } else {
                        throw new Exception("Couldn't write Diet to disk");
                    }
                } catch (Exception e) {
                    diet.setFileStatus(FileUtils.FileStatus.NOT_DOWNLOADED);
                    dietDao.updateDiet(diet);
                    Timber.e(e, "Error while downloading diet %s", diet.getFileName());
                }
            }
        }
    }
}
