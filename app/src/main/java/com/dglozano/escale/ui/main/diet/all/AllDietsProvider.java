package com.dglozano.escale.ui.main.diet.all;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class AllDietsProvider {

    @ContributesAndroidInjector(modules = AllDietsModule.class)
    public abstract AllDietsFragment provideAllDietsFragment();
}
