package com.example.dglozano.escale.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.example.dglozano.escale.data.dao.BodyMeasurementDao;
import com.example.dglozano.escale.data.dao.UserDao;
import com.example.dglozano.escale.data.entities.BodyMeasurement;
import com.example.dglozano.escale.data.entities.User;

@Database(entities = {BodyMeasurement.class, User.class}, version = 4, exportSchema = false)
@TypeConverters(DatabaseConverters.class)
public abstract class EscaleDatabase extends RoomDatabase {

    public abstract BodyMeasurementDao bodyMeasurementDao();
    public abstract UserDao userDao();
}
