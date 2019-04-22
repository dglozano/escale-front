package com.dglozano.escale.ui.doctor.main.home.alerts;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class DoctorHomeAlertListProvider {

    @ContributesAndroidInjector(modules = DoctorHomeAlertListModule.class)
    public abstract DoctorHomeAlertListFragment providesDoctorAlertListFragmentFactory();
}
