package com.dglozano.escale.di;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;

import com.dglozano.escale.db.EscaleDatabase;
import com.dglozano.escale.db.dao.BodyMeasurementDao;
import com.dglozano.escale.db.dao.UserDao;
import com.dglozano.escale.di.annotation.ApplicationContext;
import com.dglozano.escale.di.annotation.ApplicationScope;
import com.dglozano.escale.di.annotation.BluetoothInfo;
import com.dglozano.escale.di.annotation.DatabaseInfo;
import com.dglozano.escale.util.Constants;
import com.polidea.rxandroidble2.RxBleClient;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import dagger.Module;
import dagger.Provides;

@Module(includes = ViewModelModule.class)
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

    @Provides
    @ApplicationScope
    RxBleClient providesRxBleClient(@ApplicationContext Context context) {
        return RxBleClient.create(context);
    }

    @Provides
    @BluetoothInfo
    String provideScaleName() {
        // TODO: Move to App Constants
        return Constants.BF600;
    }

    @Provides
    SimpleDateFormat provideSimpleDateFormat() {
        return new SimpleDateFormat(Constants.SIMPLE_DATE_FORMAT);
    }

    @Provides
    DecimalFormat provideDecimalFormat() {
        return new DecimalFormat(Constants.DECIMAL_FORMAT);
    }
}