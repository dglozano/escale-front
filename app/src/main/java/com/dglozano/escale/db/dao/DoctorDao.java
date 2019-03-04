package com.dglozano.escale.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import com.dglozano.escale.db.entity.Doctor;
import com.dglozano.escale.db.entity.Patient;

import java.util.List;
import java.util.Optional;

import io.reactivex.Single;

@Dao
public interface DoctorDao {
    @Query("SELECT * FROM Doctor")
    LiveData<List<Doctor>> getAllDoctors();

    @Query("SELECT * FROM Doctor WHERE id == :id")
    LiveData<Doctor> getDoctorByIdAsLiveData(Long id);

    @Query("SELECT * FROM Doctor WHERE id == :id")
    Optional<Doctor> getDoctorById(Long id);

    @Delete
    void deleteDoctor(Doctor doctor);

    //FIXME: Borrar REPLACE cuando entre en produccion
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long save(Doctor doctor);

    @Query("DELETE FROM Doctor")
    void deleteAll();
}
