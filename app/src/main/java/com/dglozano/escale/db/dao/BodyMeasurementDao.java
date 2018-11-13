package com.dglozano.escale.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.dglozano.escale.db.entity.BodyMeasurement;

import java.util.List;

@Dao
public interface BodyMeasurementDao {
    @Query("SELECT * FROM BodyMeasurement ORDER BY date DESC")
    LiveData<List<BodyMeasurement>> getAllBodyMeasurement();

    @Query("SELECT * FROM BodyMeasurement WHERE userId == :userId ORDER BY date DESC")
    LiveData<List<BodyMeasurement>> getAllBodyMeasurementByUserId(Integer userId);

    @Query("SELECT * FROM BodyMeasurement WHERE id == :id")
    BodyMeasurement getBodyMeasurementById(Integer id);

    //FIXME: Borrar REPLACE cuando entre en produccion
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insertBodyMeasurement(BodyMeasurement bodyMeasurement);

    @Delete
    void deleteBodyMeasurement(BodyMeasurement bodyMeasurement);
}
