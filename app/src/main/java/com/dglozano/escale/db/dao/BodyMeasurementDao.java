package com.dglozano.escale.db.dao;

import com.dglozano.escale.db.entity.BodyMeasurement;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import io.reactivex.Single;

@Dao
public abstract class BodyMeasurementDao extends BaseDao<BodyMeasurement> {
    @Query("SELECT * FROM BodyMeasurement ORDER BY date DESC")
    public abstract LiveData<List<BodyMeasurement>> getAllBodyMeasurement();

    @Query("SELECT * FROM BodyMeasurement WHERE userId == :userId ORDER BY date DESC")
    public abstract LiveData<List<BodyMeasurement>> getAllBodyMeasurementByUserId(Long userId);

    @Query("SELECT * FROM BodyMeasurement WHERE userId == :userId ORDER BY date DESC LIMIT :limit")
    public abstract LiveData<List<BodyMeasurement>> getLastBodyMeasurementsOfUserWithId(Long userId, Integer limit);

    @Query("SELECT * FROM BodyMeasurement WHERE userId == :userId AND date >= :since ORDER BY date DESC")
    public abstract LiveData<List<BodyMeasurement>> getBodyMeasurementsOfUserWithIdSince(Long userId, Date since);

    @Query("SELECT * FROM BodyMeasurement WHERE userId == :userId ORDER BY date DESC LIMIT 1")
    public abstract LiveData<Optional<BodyMeasurement>> getLastBodyMeasurementOfUserWithIdOptional(Long userId);

    @Query("SELECT date FROM BodyMeasurement WHERE userId == :userId ORDER BY date DESC LIMIT 1")
    public abstract Single<Optional<Date>> getDateOfLastBodyMeasurement(Long userId);

    @Query("SELECT * FROM BodyMeasurement WHERE id == :id")
    public abstract BodyMeasurement getBodyMeasurementById(Integer id);

    @Query("SELECT COUNT(*) FROM BodyMeasurement WHERE id == :id")
    public abstract Integer measurementExists(Long id);

    @Query("SELECT * FROM BodyMeasurement WHERE userId == :patientId AND date <= :goalStartDate ORDER BY date DESC LIMIT 1")
    public abstract Optional<BodyMeasurement> getLastBodyMeasurementBeforeGoalStarted(Date goalStartDate, Long patientId);

    @Query("SELECT * FROM BodyMeasurement WHERE userId == :patientId AND date >= :goalStartDate ORDER BY date ASC LIMIT 1")
    public abstract Optional<BodyMeasurement> getFirstBodyMeasurementAfterGoalStarted(Date goalStartDate, Long patientId);

    @Query("SELECT * FROM BodyMeasurement WHERE userId == :loggedPatiendId ORDER BY date DESC LIMIT 1")
    public abstract Optional<BodyMeasurement> getLastBodyMeasurementBlockingOfPatient(Long loggedPatiendId);
}
