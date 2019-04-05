package com.dglozano.escale.ui.main.diet;

import androidx.fragment.app.FragmentManager;

import dagger.Module;
import dagger.Provides;

@Module
public class DietFragmentModule {

    @Provides
    static FragmentManager provideFragmentManager(DietFragment dietFragment) {
        return dietFragment.getChildFragmentManager();
    }
}
