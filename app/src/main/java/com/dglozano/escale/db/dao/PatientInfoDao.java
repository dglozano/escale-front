package com.dglozano.escale.db.dao;

import com.dglozano.escale.db.entity.PatientInfo;

import java.util.List;
import java.util.Optional;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import io.reactivex.Single;

@Dao
public abstract class PatientInfoDao extends BaseDao<PatientInfo> {
    @Query("SELECT * FROM PatientInfo WHERE doctorId == :doctorId")
    public abstract LiveData<List<PatientInfo>> getAllPatientInfoForDoctor(Long doctorId);

    @Query("SELECT * FROM PatientInfo WHERE doctorId == :doctorId")
    public abstract Single<List<PatientInfo>> getAllPatientInfoForDoctorAsSingle(Long doctorId);

    @Query("SELECT * FROM PatientInfo WHERE doctorId == :doctorId AND patientId == :patientId LIMIT 1")
    public abstract Single<Optional<PatientInfo>> getPatientInfoByPatientIdOfDoctor(Long doctorId, Long patientId);

    @Query("DELETE FROM PatientInfo WHERE doctorId == :doctorId")
    public abstract void deleteAllForDoctor(Long doctorId);

    @Query("DELETE FROM PatientInfo")
    public abstract void deleteAll();

}
