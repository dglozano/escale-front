package com.dglozano.escale.ui.main.diet.current;

import com.dglozano.escale.ui.main.diet.all.AllDietsFragment;
import com.dglozano.escale.ui.main.diet.all.AllDietsModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class CurrentDietProvider {

    @ContributesAndroidInjector(modules = CurrentDietModule.class)
    public abstract CurrentDietFragment provideCurrentDietFragment();
}
