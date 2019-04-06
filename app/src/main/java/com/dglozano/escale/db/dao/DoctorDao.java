package com.dglozano.escale.db.dao;

import com.dglozano.escale.db.entity.Doctor;

import java.util.List;
import java.util.Optional;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public abstract class DoctorDao extends BaseDao<Doctor> {
    @Query("SELECT * FROM Doctor")
    public abstract LiveData<List<Doctor>> getAllDoctors();

    @Query("SELECT * FROM Doctor WHERE id == :id")
    public abstract LiveData<Doctor> getDoctorByIdAsLiveData(Long id);

    @Query("SELECT * FROM Doctor WHERE id == :id")
    public abstract Optional<Doctor> getDoctorById(Long id);

    @Query("DELETE FROM Doctor")
    public abstract void deleteAll();
}
