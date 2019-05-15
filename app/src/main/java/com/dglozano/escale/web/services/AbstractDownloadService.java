package com.dglozano.escale.web.services;

import android.app.IntentService;
import android.content.Intent;

import com.dglozano.escale.di.annotation.RootFileDirectory;
import com.dglozano.escale.web.EscaleRestApi;

import java.io.File;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import dagger.android.AndroidInjection;

public abstract class AbstractDownloadService extends IntentService {

    @Inject
    EscaleRestApi escaleRestApi;
    @Inject
    @RootFileDirectory
    File mFileDirectory;

    public AbstractDownloadService() {
        super(AbstractDownloadService.class.getName());
        setIntentRedelivery(true);
    }

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    protected abstract void onHandleIntent(@Nullable Intent intent);
}

