package com.dglozano.escale.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.dglozano.escale.db.entity.Doctor;

import java.util.List;
import java.util.Optional;

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
