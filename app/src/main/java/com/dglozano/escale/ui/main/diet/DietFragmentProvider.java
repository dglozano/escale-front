package com.dglozano.escale.ui.main.diet;

import com.dglozano.escale.ui.main.home.HomeFragment;
import com.dglozano.escale.ui.main.home.HomeFragmentModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class DietFragmentProvider {

    @ContributesAndroidInjector(modules = DietFragmentModule.class)
    public abstract DietFragment provideDietFragmentFactory();
}
