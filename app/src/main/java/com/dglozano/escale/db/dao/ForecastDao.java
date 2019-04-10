package com.dglozano.escale.db.dao;

import com.dglozano.escale.db.entity.MeasurementForecast;
import com.dglozano.escale.db.entity.MeasurementPrediction;

import java.util.List;
import java.util.Optional;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

@Dao
public abstract class ForecastDao extends BaseDao<MeasurementForecast> {


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract Long insertPrediction(MeasurementPrediction prediction);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    public abstract void updatePrediction(MeasurementPrediction prediction);

    @Delete
    public abstract void delete(MeasurementPrediction prediction);

    public void upsertPrediction(MeasurementPrediction prediction) {
        long id = insertPrediction(prediction);
        if (id == -1) {
            updatePrediction(prediction);
        }
    }

    @Query("SELECT * FROM MeasurementForecast WHERE id =:id")
    public abstract Optional<MeasurementForecast> getForecast(Long id);

    @Query("SELECT * FROM MeasurementForecast WHERE patientId =:userId")
    public abstract Optional<MeasurementForecast> getForecastOfUser(Long userId);

    @Query("SELECT * FROM MeasurementPrediction WHERE forecastId =:forecastId")
    public abstract List<MeasurementPrediction> getPredictionsList(Long forecastId);

    @Query("SELECT * FROM MeasurementForecast WHERE patientId == :userId ORDER BY dateCalculated DESC LIMIT 1")
    public abstract Optional<MeasurementForecast> getLastForecastOfUserWithId(Long userId);

    @Query("SELECT * FROM MeasurementForecast WHERE patientId == :userId ORDER BY dateCalculated DESC LIMIT 1")
    public abstract LiveData<Optional<MeasurementForecast>> getLastForecastOfUserWithIdAsLiveData(Long userId);

    @Transaction
    public void upsertForecastWithPredictions(MeasurementForecast mf) {
        upsert(mf);
        mf.getNextDaysPredictions()
                .forEach(measurementPrediction -> {
                    measurementPrediction.setForecastId(mf.getId());
                    upsertPrediction(measurementPrediction);
                });
    }

    @Transaction
    public Optional<MeasurementForecast>  getForecastWithPredictions(Long id) {
        Optional<MeasurementForecast> mfOpt = getForecast(id);
        if (mfOpt.isPresent()) {
            MeasurementForecast mf = mfOpt.get();
            List<MeasurementPrediction> mpList = getPredictionsList(mf.getId());
            mf.setNextDaysPredictions(mpList);
            return Optional.of(mf);
        } else {
            return Optional.empty();
        }
    }

    @Transaction
    public Optional<MeasurementForecast> getForecastWithPredictionsOfUser(Long userId) {
        Optional<MeasurementForecast> mfOpt = getLastForecastOfUserWithId(userId);
        if (mfOpt.isPresent()) {
            MeasurementForecast mf = mfOpt.get();
            List<MeasurementPrediction> mpList = getPredictionsList(mf.getId());
            mf.setNextDaysPredictions(mpList);
            return Optional.of(mf);
        } else {
            return Optional.empty();
        }
    }

    @Query("DELETE FROM MeasurementForecast WHERE patientId == :userId")
    public abstract void deleteAllByUserId(Long userId);
}
