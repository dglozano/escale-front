package com.dglozano.escale;

import android.app.Activity;
import android.app.Application;

import com.dglozano.escale.di.DaggerAppComponent;

import javax.inject.Inject;

import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import timber.log.Timber;

public class EscaleApp extends Application implements HasActivityInjector {

    @Inject
    DispatchingAndroidInjector<Activity> activityDispatchingAndroidInjector;

    @Override
    public void onCreate() {

        DaggerAppComponent
                .builder()
                .application(this)
                .build()
                .inject(this);


        Timber.plant(new Timber.DebugTree());
        /*if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree()) {
                //Add the line number to the tag
                @Override
                protected @Nullable String createStackElementTag(@NotNull StackTraceElement element) {
                    return super.createStackElementTag(element) + ':' + element.getLineNumber();
                }
            });
        */
        super.onCreate();
    }

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return activityDispatchingAndroidInjector;
    }
}
