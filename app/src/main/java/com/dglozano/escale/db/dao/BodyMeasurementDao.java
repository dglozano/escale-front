package com.dglozano.escale.db.dao;

import com.dglozano.escale.db.entity.BodyMeasurement;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import io.reactivex.Single;

@Dao
public interface BodyMeasurementDao {
    @Query("SELECT * FROM BodyMeasurement ORDER BY date DESC")
    LiveData<List<BodyMeasurement>> getAllBodyMeasurement();

    @Query("SELECT * FROM BodyMeasurement WHERE userId == :userId ORDER BY date DESC")
    LiveData<List<BodyMeasurement>> getAllBodyMeasurementByUserId(Long userId);

    @Query("SELECT * FROM BodyMeasurement WHERE userId == :userId ORDER BY date DESC LIMIT :limit")
    LiveData<List<BodyMeasurement>> getLastBodyMeasurementsOfUserWithId(Long userId, Integer limit);

    @Query("SELECT * FROM BodyMeasurement WHERE userId == :userId AND date >= :since ORDER BY date DESC")
    LiveData<List<BodyMeasurement>> getBodyMeasurementsOfUserWithIdSince(Long userId, Date since);

    @Query("SELECT * FROM BodyMeasurement WHERE userId == :userId ORDER BY date DESC LIMIT 1")
    LiveData<Optional<BodyMeasurement>> getLastBodyMeasurementOfUserWithIdOptional(Long userId);

    @Query("SELECT date FROM BodyMeasurement WHERE userId == :userId ORDER BY date DESC LIMIT 1")
    Single<Optional<Date>> getDateOfLastBodyMeasurement(Long userId);

    @Query("SELECT * FROM BodyMeasurement WHERE id == :id")
    BodyMeasurement getBodyMeasurementById(Integer id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insertBodyMeasurement(BodyMeasurement bodyMeasurement);

    @Query("SELECT COUNT(*) FROM BodyMeasurement WHERE id == :id")
    Integer measurementExists(Long id);

    @Delete
    void deleteBodyMeasurement(BodyMeasurement bodyMeasurement);

    @Query("SELECT * FROM BodyMeasurement WHERE userId == :patientId AND date <= :goalStartDate ORDER BY date DESC LIMIT 1")
    Optional<BodyMeasurement> getLastBodyMeasurementBeforeGoalStarted(Date goalStartDate, Long patientId);

    @Query("SELECT * FROM BodyMeasurement WHERE userId == :patientId AND date >= :goalStartDate ORDER BY date ASC LIMIT 1")
    Optional<BodyMeasurement> getFirstBodyMeasurementAfterGoalStarted(Date goalStartDate, Long patientId);

    @Query("SELECT * FROM BodyMeasurement WHERE userId == :loggedPatiendId ORDER BY date DESC LIMIT 1")
    Optional<BodyMeasurement> getLastBodyMeasurementBlockingOfPatient(Long loggedPatiendId);
}
