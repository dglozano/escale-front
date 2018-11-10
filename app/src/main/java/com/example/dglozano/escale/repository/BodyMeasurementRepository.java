package com.example.dglozano.escale.repository;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.example.dglozano.escale.db.dao.BodyMeasurementDao;
import com.example.dglozano.escale.db.entity.BodyMeasurement;

import java.util.List;

public class BodyMeasurementRepository {

    private BodyMeasurementDao mBodyMeasurementDao;
    private LiveData<List<BodyMeasurement>> mAllBodyMeasurements;

    public BodyMeasurementRepository(BodyMeasurementDao bodyMeasurementDao) {
        mBodyMeasurementDao = bodyMeasurementDao;
    }

    public LiveData<List<BodyMeasurement>> getAllBodyMeasurementsOfUser(int userId) {
        mAllBodyMeasurements = mBodyMeasurementDao.getAllBodyMeasurementByUserId(userId);
        return mAllBodyMeasurements;
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
