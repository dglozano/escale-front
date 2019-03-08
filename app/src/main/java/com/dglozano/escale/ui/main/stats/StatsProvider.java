package com.dglozano.escale.ui.main.stats;

import com.dglozano.escale.ui.main.messages.MessagesFragment;
import com.dglozano.escale.ui.main.messages.MessagesModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class StatsProvider {

    @ContributesAndroidInjector(modules = StatsModule.class)
    public abstract StatsFragment providesStatsFragmentFactory();
}
