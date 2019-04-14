package com.dglozano.escale.ui.main.stats;

import androidx.fragment.app.FragmentManager;
import dagger.Module;
import dagger.Provides;

@Module
public class StatsFragmentModule {

    @Provides
    static FragmentManager provideFragmentManager(StatsFragment statsFragment) {
        return statsFragment.getChildFragmentManager();
    }
}
