package com.dglozano.escale.ui.main.stats;

import com.dglozano.escale.ui.main.diet.DietFragment;
import com.dglozano.escale.ui.main.diet.DietFragmentModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class StatsFragmentProvider {

    @ContributesAndroidInjector(modules = StatsFragmentModule.class)
    public abstract StatsFragment provideStatsFragmentFactory();
}
