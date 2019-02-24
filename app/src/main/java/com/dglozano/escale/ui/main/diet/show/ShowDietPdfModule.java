package com.dglozano.escale.ui.main.diet.show;

import com.dglozano.escale.ui.main.diet.CustomPdfScrollHandle;

import dagger.Module;
import dagger.Provides;

@Module
public class ShowDietPdfModule {

    @Provides
    CustomPdfScrollHandle provideScrollHandle(ShowDietPdfActivity activity) {
        return new CustomPdfScrollHandle(activity);
    }
}