package com.dglozano.escale.ui.doctor.main.add_diet;

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