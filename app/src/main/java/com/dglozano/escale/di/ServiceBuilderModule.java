package com.dglozano.escale.di;

import com.dglozano.escale.ble.BF600BleService;
import com.dglozano.escale.web.services.DietDownloadService;
import com.dglozano.escale.web.services.FirebaseTokenSenderService;
import com.dglozano.escale.web.services.MyFirebaseMessagingService;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ServiceBuilderModule {

    @ContributesAndroidInjector
    public abstract BF600BleService contributeBleService();

    @ContributesAndroidInjector
    public abstract DietDownloadService contributeDietDownloadService();

    @ContributesAndroidInjector
    public abstract FirebaseTokenSenderService contributeFirebaseTokenSenderService();

    @ContributesAndroidInjector
    public abstract MyFirebaseMessagingService contributeFirebaseMessagingService();
}