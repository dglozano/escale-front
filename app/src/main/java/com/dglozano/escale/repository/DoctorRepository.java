package com.dglozano.escale.repository;

import android.content.SharedPreferences;

import com.dglozano.escale.db.dao.DoctorDao;
import com.dglozano.escale.db.dao.PatientDao;
import com.dglozano.escale.db.dao.UserDao;
import com.dglozano.escale.db.entity.Doctor;
import com.dglozano.escale.di.annotation.ApplicationScope;
import com.dglozano.escale.web.EscaleRestApi;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;

@ApplicationScope
public class DoctorRepository {

    private PatientDao mPatientDao;
    private DoctorDao mDoctorDao;
    private UserDao mUserDao;
    private EscaleRestApi mEscaleRestApi;
    private SharedPreferences mSharedPreferences;

    @Inject
    public DoctorRepository(PatientDao patientDao, EscaleRestApi escaleRestApi, DoctorDao doctorDao,
                            UserDao userDao, SharedPreferences sharedPreferences) {
        mUserDao = userDao;
        mDoctorDao = doctorDao;
        mPatientDao = patientDao;
        mEscaleRestApi = escaleRestApi;
        mSharedPreferences = sharedPreferences;
    }

    public LiveData<Doctor> getDoctorById(Long doctorId) {
        return mDoctorDao.getDoctorByIdAsLiveData(doctorId);
    }

    public void upsert(Doctor doctor) {
        mDoctorDao.upsert(doctor);
    }
}
