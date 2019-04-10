package com.dglozano.escale.db.dao;

import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.db.entity.MeasurementForecast;

import java.util.List;
import java.util.Optional;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import io.reactivex.Single;

@Dao
public abstract class PatientDao extends BaseDao<Patient> {
    @Query("SELECT * FROM Patient")
    public abstract LiveData<List<Patient>> getAllUsers();

    @Query("SELECT * FROM Patient WHERE id == :id")
    public abstract LiveData<Patient> getPatientById(Long id);

    @Query("SELECT * FROM Patient WHERE id == :id")
    public abstract Single<Patient> getPatientSingleById(Long id);

    @Query("SELECT * FROM Patient WHERE id == :id")
    public abstract Optional<Patient> getPatientByIdOptional(Long id);

    @Query("SELECT COUNT(*) FROM Patient WHERE id == :id AND lastUpdate >= :timeout")
    public abstract Single<Integer> hasUser(Long id, long timeout);

    @Query("SELECT * FROM Patient WHERE id == :id AND lastUpdate >= :timeout")
    public abstract Single<Patient> getUserIfFresh(Long id, long timeout);

    @Query("DELETE FROM Patient WHERE id == :loggedPatiendId")
    public abstract void deleteUserById(Long loggedPatiendId);

    @Query("SELECT goalInKg FROM Patient WHERE id == :id")
    public abstract Optional<Float> getGoalOfPatient(Long id);

    @Query("DELETE FROM Patient")
    public abstract void deleteAll();

}
