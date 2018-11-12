package com.dglozano.escale.di;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;

import com.dglozano.escale.db.EscaleDatabase;
import com.dglozano.escale.db.dao.BodyMeasurementDao;
import com.dglozano.escale.db.dao.UserDao;
import com.dglozano.escale.di.annotation.ApplicationContext;
import com.dglozano.escale.di.annotation.ApplicationScope;
import com.dglozano.escale.di.annotation.DatabaseInfo;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    @Provides
    @ApplicationContext
    @ApplicationScope
    Context provideContext(Application application) {
        return application;
    }

    @Provides
    @ApplicationScope
    EscaleDatabase provideAppDatabase(@DatabaseInfo String dbName, @ApplicationContext Context context) {
        return Room.databaseBuilder(context, EscaleDatabase.class, dbName)
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    @ApplicationScope
    UserDao provideUserDao(EscaleDatabase db) {
        return db.userDao();
    }

    @Provides
    @ApplicationScope
    BodyMeasurementDao provideBodyMeasurementDao(EscaleDatabase db) {
        return db.bodyMeasurementDao();
    }

    @Provides
    @DatabaseInfo
    String provideDatabaseName() {
        // TODO: Move to App Constants
        return "escale.db";
    }
}