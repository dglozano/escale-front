package com.dglozano.escale.repository;

import com.dglozano.escale.db.dao.AlertDao;
import com.dglozano.escale.db.entity.Alert;
import com.dglozano.escale.di.annotation.ApplicationScope;
import com.dglozano.escale.web.EscaleRestApi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import io.reactivex.Completable;
import timber.log.Timber;

@ApplicationScope
public class AlertRepository {

    private AlertDao mAlertDao;
    private EscaleRestApi mEscaleRestApi;
    private PatientRepository mPatientRepository;
    private DoctorRepository mDoctorRepository;

    @Inject
    public AlertRepository(AlertDao alertDao,
                           PatientRepository patientRepository,
                           DoctorRepository doctorRepository,
                           EscaleRestApi escaleRestApi) {
        mPatientRepository = patientRepository;
        mAlertDao = alertDao;
        mEscaleRestApi = escaleRestApi;
        mDoctorRepository = doctorRepository;
    }

    public LiveData<List<Alert>> getAllPatientAlertForDoctor(Long doctorId, Long patientId) {
        return mAlertDao.getAllPatientAlertForDoctor(doctorId, patientId);
    }

    public Completable upsertAlert(Long id, Long patient_id, Long doctor_id, int alert_type,
                                   String alert_msg, String dateString) {
        try {
            String format = "yyyy-MM-dd'T'HH:mm:ss.SSS";
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date date = sdf.parse(dateString);
            Alert alert = new Alert(id, patient_id, doctor_id, alert_type, alert_msg, date);
            mAlertDao.upsert(alert);
            return Completable.complete();
        } catch (ParseException e) {
            Timber.e("e");
            return Completable.error(e);
        }
    }
}
