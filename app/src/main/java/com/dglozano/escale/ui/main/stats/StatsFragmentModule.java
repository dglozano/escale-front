package com.dglozano.escale.ui.main.stats;

import android.support.v4.app.FragmentManager;

import com.dglozano.escale.ui.main.diet.DietFragment;

import dagger.Module;
import dagger.Provides;

@Module
public class StatsFragmentModule {

    @Provides
    static FragmentManager provideFragmentManager(StatsFragment statsFragment) {
        return statsFragment.getChildFragmentManager();
    }
}
