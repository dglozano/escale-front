package com.dglozano.escale.ui.main.diet;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class DietFragmentProvider {

    @ContributesAndroidInjector(modules = DietFragmentModule.class)
    public abstract DietFragment provideDietFragmentFactory();
}
