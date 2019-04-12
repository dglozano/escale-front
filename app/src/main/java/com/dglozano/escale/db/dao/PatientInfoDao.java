package com.dglozano.escale.db.dao;

import com.dglozano.escale.db.entity.PatientInfo;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

@Dao
public abstract class PatientInfoDao extends BaseDao<PatientInfo> {
    @Query("SELECT * FROM PatientInfo WHERE doctorId == :doctorId")
    public abstract LiveData<List<PatientInfo>> getAllPatientInfoForDoctor(Long doctorId);

    @Query("DELETE FROM PatientInfo WHERE doctorId == :doctorId")
    public abstract void deleteAllForDoctor(Long doctorId);

    @Query("DELETE FROM PatientInfo")
    public abstract void deleteAll();

}
