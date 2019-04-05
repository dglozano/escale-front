package com.dglozano.escale.ui.main.diet.all;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dglozano.escale.ui.main.MainActivity;

import dagger.Module;
import dagger.Provides;

@Module
public class AllDietsModule {

    @Provides
    DividerItemDecoration provideDividerItemDecoration(MainActivity mainActivity) {
        return new DividerItemDecoration(mainActivity, LinearLayoutManager.VERTICAL);
    }

    @Provides
    DefaultItemAnimator provideDefaultItemAnimator() {
        return new DefaultItemAnimator();
    }
}
