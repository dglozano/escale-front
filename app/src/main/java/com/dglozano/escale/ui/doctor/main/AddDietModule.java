package com.dglozano.escale.ui.doctor.main;

import com.dglozano.escale.util.ui.CustomPdfScrollHandle;

import dagger.Module;
import dagger.Provides;

@Module
public class AddDietModule {

    @Provides
    CustomPdfScrollHandle provideScrollHandle(AddDietActivity activity) {
        return new CustomPdfScrollHandle(activity);
    }
}