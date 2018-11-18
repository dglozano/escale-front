package com.dglozano.escale;

import android.app.Activity;
import android.app.Application;
import android.app.Service;

import com.dglozano.escale.di.DaggerAppComponent;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;
import timber.log.Timber;

public class EscaleApp extends Application implements HasActivityInjector, HasServiceInjector {

    @Inject
    DispatchingAndroidInjector<Activity> activityDispatchingAndroidInjector;
    @Inject
    DispatchingAndroidInjector<Service> dispatchingServiceInjector;

    @Override
    public void onCreate() {

        DaggerAppComponent
                .builder()
                .application(this)
                .build()
                .inject(this);

        //Timber.plant(new Timber.DebugTree());

        Timber.plant(new Timber.DebugTree() {
            //Add the line number to the tag
            @Override
            protected @Nullable
            String createStackElementTag(@NotNull StackTraceElement element) {
                return "BleDebug - " + super.createStackElementTag(element) + '@' + element.getLineNumber();
            }
        });

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

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return dispatchingServiceInjector;
    }
}
