package com.dglozano.escale.ui.main.stats;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class StatsFragmentProvider {

    @ContributesAndroidInjector(modules = StatsFragmentModule.class)
    public abstract StatsFragment provideStatsFragmentFactory();
}
