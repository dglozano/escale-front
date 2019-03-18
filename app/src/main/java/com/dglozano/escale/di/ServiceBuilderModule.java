package com.dglozano.escale.di;

import com.dglozano.escale.ble.BleCommunicationService;
import com.dglozano.escale.web.services.DownloadService;
import com.dglozano.escale.web.services.FirebaseTokenSenderService;
import com.dglozano.escale.web.services.MyFirebaseMessagingService;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ServiceBuilderModule {

    @ContributesAndroidInjector
    public abstract BleCommunicationService contributeBleService();

    @ContributesAndroidInjector
    public abstract DownloadService contributeDownloadService();

    @ContributesAndroidInjector
    public abstract FirebaseTokenSenderService contributeFirebaseTokenSenderService();

    @ContributesAndroidInjector
    public abstract MyFirebaseMessagingService contributeFirebaseMessagingService();
}