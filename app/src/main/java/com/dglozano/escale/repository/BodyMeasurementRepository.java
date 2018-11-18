package com.dglozano.escale.repository;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.dglozano.escale.db.dao.BodyMeasurementDao;
import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.di.annotation.ApplicationScope;

import java.util.List;

import javax.inject.Inject;

@ApplicationScope
public class BodyMeasurementRepository {

    private BodyMeasurementDao mBodyMeasurementDao;

    @Inject
    public BodyMeasurementRepository(BodyMeasurementDao bodyMeasurementDao) {
        mBodyMeasurementDao = bodyMeasurementDao;
    }

    public LiveData<List<BodyMeasurement>> getAllBodyMeasurements() {
        return mBodyMeasurementDao.getAllBodyMeasurement();
    }

    public LiveData<List<BodyMeasurement>> getAllBodyMeasurementsOfUser(int userId) {
        return mBodyMeasurementDao.getAllBodyMeasurementByUserId(userId);
    }

    public LiveData<BodyMeasurement> getLastBodyMeasurementOfUserWithId(int userId) {
        return mBodyMeasurementDao.getLastBodyMeasurementOfUserWithId(userId);
    }

    public void insert(BodyMeasurement bodyMeasurement) {
        new insertAsyncTask(mBodyMeasurementDao).execute(bodyMeasurement);
    }

    private static class insertAsyncTask extends AsyncTask<BodyMeasurement, Void, Void> {

        private BodyMeasurementDao mAsyncTaskDao;

        insertAsyncTask(BodyMeasurementDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final BodyMeasurement... params) {
            mAsyncTaskDao.insertBodyMeasurement(params[0]);
            return null;
        }
    }
}
