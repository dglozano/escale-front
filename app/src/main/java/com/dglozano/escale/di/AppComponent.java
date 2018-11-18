package com.dglozano.escale.di;

import android.app.Application;

import com.dglozano.escale.EscaleApp;
import com.dglozano.escale.di.annotation.ApplicationScope;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

@ApplicationScope
@Component(modules = {
        AndroidInjectionModule.class,
        AppModule.class,
        ActivityBuilder.class,
        ServiceBuilderModule.class
})
public interface AppComponent {

    void inject(EscaleApp app);

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }
}
