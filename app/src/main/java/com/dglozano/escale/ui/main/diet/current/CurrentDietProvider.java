package com.dglozano.escale.ui.main.diet.current;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class CurrentDietProvider {

    @ContributesAndroidInjector(modules = CurrentDietModule.class)
    public abstract CurrentDietFragment provideCurrentDietFragment();
}
