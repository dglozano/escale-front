package com.dglozano.escale.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.dglozano.escale.db.dao.BodyMeasurementDao;
import com.dglozano.escale.db.dao.DietDao;
import com.dglozano.escale.db.dao.PatientDao;
import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.db.entity.Diet;
import com.dglozano.escale.db.entity.Patient;

@Database(entities = {BodyMeasurement.class, Patient.class, Diet.class}, version = 8, exportSchema = false)
@TypeConverters(DatabaseConverters.class)
public abstract class EscaleDatabase extends RoomDatabase {

    public abstract BodyMeasurementDao bodyMeasurementDao();

    public abstract PatientDao userDao();

    public abstract DietDao dietDao();
}
