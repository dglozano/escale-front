package com.dglozano.escale.ui.doctor.main.home.profile;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class DoctorHomeProfileFragmentProvider {

    @ContributesAndroidInjector(modules = DoctorHomeProfileFragmentModule.class)
    public abstract DoctorHomeProfileFragment provideHomeDoctorProfileFragmentFactory();
}
