package com.dglozano.escale.ui.main.diet.old;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class OldDietsProvider {

    @ContributesAndroidInjector(modules = OldDietsModule.class)
    public abstract OldDietsFragment provideOldDietsFragment();
}
