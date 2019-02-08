package com.dglozano.escale.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.dglozano.escale.db.dao.BodyMeasurementDao;
import com.dglozano.escale.db.dao.PatientDao;
import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.db.entity.Patient;

@Database(entities = {BodyMeasurement.class, Patient.class}, version = 6, exportSchema = false)
@TypeConverters(DatabaseConverters.class)
public abstract class EscaleDatabase extends RoomDatabase {

    public abstract BodyMeasurementDao bodyMeasurementDao();

    public abstract PatientDao userDao();
}
