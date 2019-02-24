package com.dglozano.escale.ui.main.diet.old;

import com.dglozano.escale.ui.main.diet.CustomPdfScrollHandle;

import dagger.Module;
import dagger.Provides;

@Module
public class OldDietPdfModule {

    @Provides
    CustomPdfScrollHandle provideScrollHandle(OldDietPdfActivity activity) {
        return new CustomPdfScrollHandle(activity);
    }
}