package com.dglozano.escale.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.dglozano.escale.db.entity.Patient;

import java.util.List;
import java.util.Optional;

import io.reactivex.Single;

@Dao
public interface PatientDao {
    @Query("SELECT * FROM Patient")
    LiveData<List<Patient>> getAllUsers();

    @Query("SELECT * FROM Patient WHERE id == :id")
    LiveData<Patient> getPatientById(Long id);

    @Query("SELECT * FROM Patient WHERE id == :id")
    Single<Patient> getPatientSingleById(Long id);

    @Query("SELECT * FROM Patient WHERE id == :id")
    Optional<Patient> getPatientByIdOptional(Long id);

    @Delete
    void deleteUser(Patient patient);

    //FIXME: Borrar REPLACE cuando entre en produccion
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long save(Patient patient);

    @Update
    void update(Patient patient);

    @Query("DELETE FROM Patient")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM Patient WHERE id == :id AND lastUpdate >= :timeout")
    Single<Integer> hasUser(Long id, long timeout);

    @Query("SELECT * FROM Patient WHERE id == :id AND lastUpdate >= :timeout")
    Single<Patient> getUserIfFresh(Long id, long timeout);

    @Query("DELETE FROM Patient WHERE id == :loggedPatiendId")
    void deleteUserById(Long loggedPatiendId);

}
