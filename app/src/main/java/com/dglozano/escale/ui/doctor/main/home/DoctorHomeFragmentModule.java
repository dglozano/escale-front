package com.dglozano.escale.ui.doctor.main.home;

import androidx.fragment.app.FragmentManager;
import dagger.Module;
import dagger.Provides;

@Module
public class DoctorHomeFragmentModule {

    @Provides
    static FragmentManager provideFragmentManager(DoctorHomeFragment doctorHomeFragment) {
        return doctorHomeFragment.getChildFragmentManager();
    }
}
