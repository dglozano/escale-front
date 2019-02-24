package com.dglozano.escale.ui.main.diet.current;

import com.dglozano.escale.ui.main.MainActivity;
import com.dglozano.escale.ui.main.diet.CustomPdfScrollHandle;

import dagger.Module;
import dagger.Provides;

@Module
public class CurrentDietModule {
    @Provides
    CustomPdfScrollHandle provideScrollHandle(MainActivity activity) {
        return new CustomPdfScrollHandle(activity);
    }
}
