package com.dglozano.escale.ui.doctor.main.home;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class DoctorHomeFragmentProvider {

    @ContributesAndroidInjector(modules = DoctorHomeFragmentModule.class)
    public abstract DoctorHomeFragment provideDoctorHomeFragmentFactory();
}
