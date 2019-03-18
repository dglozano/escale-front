package com.dglozano.escale.ui.main.stats.chart;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class StatsChartProvider {

    @ContributesAndroidInjector(modules = StatsChartModule.class)
    public abstract StatsChartFragment providesStatsChartFragmentFactory();
}
