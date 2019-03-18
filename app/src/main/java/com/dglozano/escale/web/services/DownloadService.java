package com.dglozano.escale.web.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.dglozano.escale.db.dao.DietDao;
import com.dglozano.escale.db.entity.Diet;
import com.dglozano.escale.di.annotation.RootFileDirectory;
import com.dglozano.escale.util.AppExecutors;
import com.dglozano.escale.util.FileUtil;
import com.dglozano.escale.web.EscaleRestApi;

import java.io.File;
import java.util.Optional;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public class DownloadService extends IntentService {

    @Inject
    DietDao dietDao;
    @Inject
    EscaleRestApi escaleRestApi;
    @Inject
    @RootFileDirectory
    File mFileDirectory;
    @Inject
    AppExecutors mAppExecutors;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    public DownloadService() {
        super(DownloadService.class.getName());
        setIntentRedelivery(true);
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
        Timber.d("Starting Download Service");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("Destroying Download Service");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Timber.d("Download Service onStartcommand - Intent %s", intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Timber.d("Handling intent in Download Service %s", intent);
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
                    boolean writtenToDisk = FileUtil.writeResponseBodyToDisk(response.body(),
                            mFileDirectory,
                            diet.getLocalFileName());
                    Timber.d("File download was a success? %s", writtenToDisk);
                    if (writtenToDisk) {
                        diet.setFileStatus(Diet.FileStatus.DOWNLOADED);
                        dietDao.updateDiet(diet);
                    } else {
                        throw new Exception("Couldn't write Diet to disk");
                    }
                } catch (Exception e) {
                    diet.setFileStatus(Diet.FileStatus.NOT_DOWNLOADED);
                    dietDao.updateDiet(diet);
                    Timber.e(e, "Error while downloading diet %s", diet.getFileName());
                }
            }
        }
    }
}
