package com.dglozano.escale.ui.doctor.main;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import dagger.Module;
import dagger.Provides;

@Module
public class DoctorMainActivityModule {

    @Provides
    DividerItemDecoration provideDividerItemDecoration(DoctorMainActivity doctorMainActivity) {
        return new DividerItemDecoration(doctorMainActivity, LinearLayoutManager.VERTICAL);
    }

    @Provides
    DefaultItemAnimator provideDefaultItemAnimator() {
        return new DefaultItemAnimator();
    }
}