package com.dglozano.escale.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.dglozano.escale.db.entity.Patient;

import java.util.List;

@Dao
public interface PatientDao {
    @Query("SELECT * FROM Patient")
    LiveData<List<Patient>> getAllUsers();

    @Query("SELECT * FROM Patient WHERE id == :id")
    LiveData<Patient> getPatientById(int id);

    @Delete
    void deleteUser(Patient patient);

    //FIXME: Borrar REPLACE cuando entre en produccion
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long save(Patient patient);

    @Query("DELETE FROM Patient")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM Patient WHERE id == :id AND lastUpdate >= :timeout")
    int hasUser(int id, long timeout);
}
