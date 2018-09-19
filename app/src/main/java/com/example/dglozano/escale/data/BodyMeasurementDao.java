package com.example.dglozano.escale.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface BodyMeasurementDao {
    @Query("SELECT * FROM BodyMeasurement ORDER BY date DESC")
    List<BodyMeasurement> getAllBodyMeasurement();

    @Query("SELECT * FROM BodyMeasurement WHERE userId == :userId ORDER BY date DESC")
    List<BodyMeasurement> getAllBodyMeasurementByUserId(Integer userId);

    @Query("SELECT * FROM BodyMeasurement WHERE id == :id")
    BodyMeasurement getBodyMeasurementById(Integer id);

    //FIXME: Borrar REPLACE cuando entre en produccion
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insertBodyMeasurement(BodyMeasurement bodyMeasurement);

    @Delete
    void deleteBodyMeasurement(BodyMeasurement bodyMeasurement);
}
