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
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@ApplicationScope
public class AlertRepository {

    private AlertDao mAlertDao;
    private EscaleRestApi mEscaleRestApi;

    @Inject
    public AlertRepository(AlertDao alertDao,
                           EscaleRestApi escaleRestApi) {
        mAlertDao = alertDao;
        mEscaleRestApi = escaleRestApi;
    }

    public LiveData<List<Alert>> getAllPatientAlertForDoctor(Long doctorId, Long patientId) {
        return mAlertDao.getAllPatientAlertForDoctor(doctorId, patientId);
    }

    public LiveData<Integer> getCountOfPatientAlertsNotSeenByDoctor(Long doctorId, Long patientId) {
        return mAlertDao.getCountOfPatientAlertsNotSeenByDoctor(doctorId, patientId);
    }

    public Completable refreshAlertsOfPatient(Long patiendId) {
        return mEscaleRestApi.getAllAlertsOfPatient(patiendId)
                .flatMapCompletable(alerts -> {
                    alerts.forEach(alert -> {
                        mAlertDao.upsert(alert);
                    });
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.io());
    }


    public Completable upsertAlert(Long id, Long patient_id, Long doctor_id, Alert.AlertType alert_type,
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

    public Completable toggleSeenByDoctor(Alert alert) {
        return Completable.fromAction(() -> {
            Timber.d("Updating state of alert locally");
            alert.setSeenByDoctor(!alert.isSeenByDoctor());
            mAlertDao.upsert(alert);
        })
                .andThen(mEscaleRestApi.toggleSeenByDoctor(alert.getPatientId(), alert.getId()))
                .doOnError(e -> {
                    Timber.d("Reverting on error state of alert locally");
                    // TODO
                });
    }

    public Completable markAllAsSeen(Long doctorId, Long patientId) {
        return mAlertDao.getAllPatientAlertUnseenByDoctorAsSingle(doctorId, patientId)
                .flatMapCompletable(unseenAlerts -> Completable.fromAction(() -> unseenAlerts.forEach(alert -> {
                    alert.setSeenByDoctor(true);
                    mAlertDao.upsert(alert);
                })))
                .andThen(mEscaleRestApi.markAllAlertsAsSeenByDoctor(patientId))
                .doOnError(e -> {
                    Timber.d("Reverting on error state of all alerts locally");
                    // TODO
                });
    }
}
