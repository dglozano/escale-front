package com.dglozano.escale.ui.main.stats.list;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dglozano.escale.ui.main.MainActivity;

import dagger.Module;
import dagger.Provides;

@Module
public class StatsListModule {

    @Provides
    DividerItemDecoration provideDividerItemDecoration(MainActivity mainActivity) {
        return new DividerItemDecoration(mainActivity, LinearLayoutManager.VERTICAL);
    }

    @Provides
    DefaultItemAnimator provideDefaultItemAnimator() {
        return new DefaultItemAnimator();
    }
}
