package com.dglozano.escale.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.dglozano.escale.db.entity.Patient;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface PatientDao {
    @Query("SELECT * FROM Patient")
    LiveData<List<Patient>> getAllUsers();

    @Query("SELECT * FROM Patient WHERE id == :id")
    LiveData<Patient> getPatientById(Long id);

    @Delete
    void deleteUser(Patient patient);

    //FIXME: Borrar REPLACE cuando entre en produccion
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long save(Patient patient);

    @Query("DELETE FROM Patient")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM Patient WHERE id == :id AND lastUpdate >= :timeout")
    Single<Integer> hasUser(Long id, long timeout);

    @Query("SELECT * FROM Patient WHERE id == :id AND lastUpdate >= :timeout")
    Single<Patient> getUserIfFresh(Long id, long timeout);
}
