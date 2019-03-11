package com.dglozano.escale.repository;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.dglozano.escale.db.dao.BodyMeasurementDao;
import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.db.entity.Diet;
import com.dglozano.escale.di.annotation.ApplicationScope;
import com.dglozano.escale.web.DateSerializer;
import com.dglozano.escale.web.EscaleRestApi;
import com.dglozano.escale.web.dto.DietDTO;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.reactivex.Completable;
import timber.log.Timber;

import static com.dglozano.escale.util.Constants.BODY_MEASUREMENTS_DEFAULT_LIMIT;

@ApplicationScope
public class BodyMeasurementRepository {

    private BodyMeasurementDao mBodyMeasurementDao;
    private EscaleRestApi mEscaleRestApi;

    @Inject
    public BodyMeasurementRepository(BodyMeasurementDao bodyMeasurementDao,
                                     EscaleRestApi escaleRestApi) {
        mBodyMeasurementDao = bodyMeasurementDao;
        mEscaleRestApi = escaleRestApi;
    }

    public LiveData<List<BodyMeasurement>> getLastBodyMeasurementsOfUserWithId(Long userId, Integer limit) {
        return mBodyMeasurementDao.getLastBodyMeasurementsOfUserWithId(userId, limit);
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
}
