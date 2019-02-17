package com.dglozano.escale.ui.main;

import android.support.design.widget.AppBarLayout;

import com.dglozano.escale.R;
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