package com.dglozano.escale.di;

import com.dglozano.escale.ble.BleCommunicationService;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ServiceBuilderModule {

    @ContributesAndroidInjector
    public abstract BleCommunicationService contributeMyService();
}