package com.dglozano.escale.ui.main.diet;

import android.support.v4.app.FragmentManager;

import dagger.Module;
import dagger.Provides;

@Module
public class DietFragmentModule {

//    @Provides
//    static List<Fragment> provideFragmentList(CurrentDietFragment currentDietFragment, NewsFragment ) {
//        List<Fragment> fragments = new ArrayList<>();
//        fragments.add(home);
//        fragments.add(news);
//        fragments.add(moments);
//        fragments.add(wallet);
//        fragments.add(personal);
//        return fragments;
//    }

    @Provides
    static FragmentManager provideFragmentManager(DietFragment dietFragment) {
        return dietFragment.getChildFragmentManager();
    }
}
