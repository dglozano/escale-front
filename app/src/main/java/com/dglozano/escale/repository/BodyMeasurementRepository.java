package com.dglozano.escale.repository;

import com.dglozano.escale.db.dao.BodyMeasurementDao;
import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.di.annotation.ApplicationScope;
import com.dglozano.escale.web.DateSerializer;
import com.dglozano.escale.web.EscaleRestApi;
import com.dglozano.escale.web.dto.AddBodyMeasurementDTO;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.dglozano.escale.util.Constants.BODY_MEASUREMENTS_DEFAULT_LIMIT;

@ApplicationScope
public class BodyMeasurementRepository {

    private BodyMeasurementDao mBodyMeasurementDao;
    private EscaleRestApi mEscaleRestApi;
    private PatientRepository mPatientRepository;

    @Inject
    public BodyMeasurementRepository(BodyMeasurementDao bodyMeasurementDao,
                                     PatientRepository patientRepository,
                                     EscaleRestApi escaleRestApi) {
        mPatientRepository = patientRepository;
        mBodyMeasurementDao = bodyMeasurementDao;
        mEscaleRestApi = escaleRestApi;
    }

    public LiveData<List<BodyMeasurement>> getBodyMeasurementsOfUserWithIdSince(Long userId, Date date) {
        return mBodyMeasurementDao.getBodyMeasurementsOfUserWithIdSince(userId, date);
    }

    public LiveData<Optional<BodyMeasurement>> getLastBodyMeasurementOfUserWithId(Long userId) {
        return mBodyMeasurementDao.getLastBodyMeasurementOfUserWithIdOptional(userId);
    }

    public Completable refreshMeasurements(Long patientId) {
        return mBodyMeasurementDao.getDateOfLastBodyMeasurement(patientId)
                .flatMap(date -> mEscaleRestApi.getLastBodyMeasurements(
                        patientId,
                        date.map(DateSerializer::formatDate).orElse(null),
                        BODY_MEASUREMENTS_DEFAULT_LIMIT))
                .flatMapCompletable(bodyMeasurementsApi -> {
                    Timber.d("Retrieved bodyMeasurements for user with id %s from Api", patientId);
                    bodyMeasurementsApi.stream()
                            .filter(measurementDTO
                                    -> mBodyMeasurementDao.measurementExists(measurementDTO.getId()) != 1)
                            .map(BodyMeasurement::new)
                            .forEach(mBodyMeasurementDao::insertBodyMeasurement);
                    return Completable.complete();
                });
    }

    public Single<Long> addMeasurement(float weight, float water, float fat, float bmi, float bones,
                                       float muscle, boolean isManual) {
        Long patientId = mPatientRepository.getLoggedPatiendId();
        AddBodyMeasurementDTO addDto = new AddBodyMeasurementDTO(patientId, weight, water,
                fat, bmi, bones, muscle, Calendar.getInstance().getTime(), isManual);
        return mEscaleRestApi.postNewMeasurement(addDto, patientId)
                .map(BodyMeasurement::new)
                .map(mBodyMeasurementDao::insertBodyMeasurement)
                .subscribeOn(Schedulers.io());
    }

    public Single<Long> addMeasurement(BodyMeasurement bodyMeasurement) {
        return addMeasurement(bodyMeasurement.getWeight(),
                bodyMeasurement.getWater(),
                bodyMeasurement.getFat(),
                bodyMeasurement.getBmi(),
                bodyMeasurement.getBones(),
                bodyMeasurement.getMuscles(),
                bodyMeasurement.isManual());
    }

    public LiveData<List<BodyMeasurement>> getLastBodyMeasurementsOfUserWithId(Long loggedPatiendId) {
        return mBodyMeasurementDao.getLastBodyMeasurementsOfUserWithId(loggedPatiendId, BODY_MEASUREMENTS_DEFAULT_LIMIT);
    }

    public Optional<BodyMeasurement> getLastBodyMeasurementBeforeGoalStarted(Date goalStartDate, Long patientId) {
        return mBodyMeasurementDao.getLastBodyMeasurementBeforeGoalStarted(goalStartDate, patientId);
    }

    public Optional<BodyMeasurement> getFirstBodyMeasurementAfterGoalStarted(Date goalStartDate, Long patientId) {
        return mBodyMeasurementDao.getFirstBodyMeasurementAfterGoalStarted(goalStartDate, patientId);
    }

    public Optional<BodyMeasurement> getLastBodyMeasurementBlockingOfPatient(Long loggedPatiendId) {
        return mBodyMeasurementDao.getLastBodyMeasurementBlockingOfPatient(loggedPatiendId);
    }
}
