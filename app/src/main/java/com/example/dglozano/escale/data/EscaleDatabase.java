package com.example.dglozano.escale.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

@Database(entities = {BodyMeasurement.class, User.class}, version = 3, exportSchema = false)
@TypeConverters(DatabaseConverters.class)
public abstract class EscaleDatabase extends RoomDatabase {

    private static final String DB_NAME = "EscaleDatabase";
    private static EscaleDatabase instance;

    public static synchronized EscaleDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context,
                    EscaleDatabase.class,
                    DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract BodyMeasurementDao bodyMeasurementDao();
    public abstract UserDao userDao();
}
