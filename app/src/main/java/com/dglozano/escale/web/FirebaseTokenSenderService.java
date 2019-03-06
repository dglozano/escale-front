package com.dglozano.escale.web;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.dglozano.escale.db.dao.DietDao;
import com.dglozano.escale.db.entity.Diet;
import com.dglozano.escale.di.annotation.RootFileDirectory;
import com.dglozano.escale.util.AppExecutors;
import com.dglozano.escale.util.Constants;
import com.dglozano.escale.util.FileUtil;
import com.dglozano.escale.web.dto.FirebaseTokenUpdateDTO;

import java.io.File;
import java.util.Optional;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public class FirebaseTokenSenderService extends IntentService {

    @Inject
    EscaleRestApi escaleRestApi;
    @Inject
    SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    public FirebaseTokenSenderService() {
        super(FirebaseTokenSenderService.class.getName());
        setIntentRedelivery(true);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Timber.d("Handling intent in Firebase Token Sender Service %s", intent);
        if (intent != null) {
            String token = intent.getStringExtra("token");
            Long patientId = intent.getLongExtra("patientId", -1L);
            if (patientId != -1L) {
                Call<Void> call = escaleRestApi.updateToken(patientId, new FirebaseTokenUpdateDTO(token));
                try {
                    Response<Void> response = call.execute();
                    if (!response.isSuccessful())
                        throw new Exception("Response was not successful");
                    Timber.d("Firebase token sent succesfully %s for user %s", token, patientId);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(Constants.IS_FIREBASE_TOKEN_SENT_SHARED_PREF, true);
                    editor.apply();
                } catch (Exception e) {
                    Timber.e(e, "Error while sending token %s for user %s", token, patientId);
                }
            }
        }
    }
}
