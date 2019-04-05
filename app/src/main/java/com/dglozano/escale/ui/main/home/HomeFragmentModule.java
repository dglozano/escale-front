package com.dglozano.escale.ui.main.home;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dglozano.escale.ui.main.MainActivity;

import dagger.Module;
import dagger.Provides;

@Module
public class HomeFragmentModule {

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
