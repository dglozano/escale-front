package com.dglozano.escale.ui.main;

import com.dglozano.escale.util.ui.BottomBarAdapter;

import dagger.Module;
import dagger.Provides;

@Module
public class MainActivityModule {

    @Provides
    BottomBarAdapter provideBottomBarAdapter(MainActivity activity) {
        return new BottomBarAdapter(activity.getSupportFragmentManager(), activity);
    }
}