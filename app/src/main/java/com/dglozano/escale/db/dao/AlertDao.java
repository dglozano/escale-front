package com.dglozano.escale.db.dao;

import com.dglozano.escale.db.entity.Alert;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import io.reactivex.Single;

@Dao
public abstract class AlertDao extends BaseDao<Alert> {
    @Query("SELECT * FROM Alert WHERE doctorId == :doctorId AND patientId == :patientId ORDER BY dateCreated DESC")
    public abstract LiveData<List<Alert>> getAllPatientAlertForDoctor(Long doctorId, Long patientId);

    @Query("SELECT * FROM Alert WHERE doctorId == :doctorId AND patientId == :patientId")
    public abstract Single<List<Alert>> getAllPatientAlertForDoctorAsSingle(Long doctorId, Long patientId);

    @Query("DELETE FROM Alert")
    public abstract void deleteAll();

    @Query("SELECT COUNT(*) FROM Alert WHERE doctorId == :doctorId AND patientId == :patientId AND seenByDoctor == 0")
    public abstract LiveData<Integer> getCountOfPatientAlertsNotSeenByDoctor(Long doctorId, Long patientId);

    @Query("SELECT * FROM Alert WHERE doctorId == :doctorId AND patientId == :patientId AND seenByDoctor == 0")
    public abstract Single<List<Alert>> getAllPatientAlertUnseenByDoctorAsSingle(Long doctorId, Long patientId);
}
