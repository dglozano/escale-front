package com.dglozano.escale.di;

import com.dglozano.escale.ui.login.LoginActivity;
import com.dglozano.escale.ui.login.LoginActivityModule;
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

    @ContributesAndroidInjector(modules = {
            LoginActivityModule.class
    })
    public abstract LoginActivity bindLoginActivity();
}