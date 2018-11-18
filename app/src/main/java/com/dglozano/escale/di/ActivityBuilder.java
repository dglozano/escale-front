package com.dglozano.escale.di;

import android.app.Service;

import com.dglozano.escale.ble.BleCommunicationService;
import com.dglozano.escale.ui.main.MainActivity;
import com.dglozano.escale.ui.main.MainActivityModule;
import com.dglozano.escale.ui.main.home.HomeFragmentProvider;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuilder {

    @ContributesAndroidInjector(modules = {
            MainActivityModule.class,
            HomeFragmentProvider.class
    })
    public abstract MainActivity bindMainActivity();
}