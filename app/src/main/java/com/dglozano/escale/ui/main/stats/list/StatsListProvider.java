package com.dglozano.escale.ui.main.stats.list;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class StatsListProvider {

    @ContributesAndroidInjector(modules = StatsListModule.class)
    public abstract StatsListFragment providesStatsListFragmentFactory();
}
