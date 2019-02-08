package com.dglozano.escale.repository;

import android.arch.lifecycle.LiveData;

import com.dglozano.escale.db.dao.PatientDao;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.di.annotation.ApplicationScope;
import com.dglozano.escale.util.AppExecutors;
import com.dglozano.escale.web.EscaleRestApi;
import com.dglozano.escale.web.dto.PatientDTO;

import java.io.IOException;
import java.util.Calendar;

import javax.inject.Inject;

import retrofit2.Response;
import timber.log.Timber;

import static com.dglozano.escale.util.Constants.FRESH_TIMEOUT;

@ApplicationScope
public class PatientRepository {

    private PatientDao mPatientDao;
    private EscaleRestApi mEscaleRestApi;
    private AppExecutors mAppExecutors;

    @Inject
    public PatientRepository(PatientDao patientDao, EscaleRestApi escaleRestApi, AppExecutors executors) {
        mPatientDao = patientDao;
        mEscaleRestApi = escaleRestApi;
        mAppExecutors = executors;
    }

    public LiveData<Patient> getPatientById(int userId) {
        refreshUser(userId);
        return mPatientDao.getPatientById(userId);
    }

    private void refreshUser(final int userId) {
        // Runs in a background thread.
        mAppExecutors.getDiskIO().execute(() -> {
            // Check if user data was fetched recently.
            boolean userExists = mPatientDao.hasUser(userId, FRESH_TIMEOUT) != 0;
            Timber.d("Does user exist and is fresh? " + userExists);
            if (!userExists) {
                // Refreshes the data.
                Response<PatientDTO> response = null;
                try {
                    response = mEscaleRestApi.getPatientById(userId).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // TODO
                // Check for errors here.

                Patient patient = new Patient(response.body(), Calendar.getInstance().getTime());

                // Updates the database. The LiveData object automatically
                // refreshes, so we don't need to do anything else here.
                mPatientDao.save(patient);
            }
        });
    }
}
