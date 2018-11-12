package com.dglozano.escale.ui.main.home;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;

import com.dglozano.escale.ui.main.MainActivity;

import dagger.Module;
import dagger.Provides;

@Module
public class HomeFragmentModule {

    @Provides
    MeasurementListAdapter provideMeasurementListAdapter(MainActivity mainActivity) {
        return new MeasurementListAdapter(mainActivity);
    }

    @Provides
    LinearLayoutManager provideLinearLayoutManager(HomeFragment fragment) {
        return new LinearLayoutManager(fragment.getActivity());
    }

    @Provides
    DividerItemDecoration provideDividerItemDecoration(MainActivity mainActivity) {
        return new DividerItemDecoration(mainActivity, LinearLayoutManager.VERTICAL);
    }

    @Provides
    DefaultItemAnimator provideDefaultItemAnimator() {
        return new DefaultItemAnimator();
    }
}
