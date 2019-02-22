package com.dglozano.escale.ui.main;

import com.dglozano.escale.ui.main.common.BottomBarAdapter;

import dagger.Module;
import dagger.Provides;

@Module
public class MainActivityModule {

    @Provides
    BottomBarAdapter provideBottomBarAdapter(MainActivity activity) {
        return new BottomBarAdapter(activity.getSupportFragmentManager(), activity);
    }
}