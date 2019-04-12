package com.dglozano.escale.repository;

import android.content.SharedPreferences;

import com.dglozano.escale.db.dao.DoctorDao;
import com.dglozano.escale.db.dao.PatientDao;
import com.dglozano.escale.db.dao.PatientInfoDao;
import com.dglozano.escale.db.dao.UserDao;
import com.dglozano.escale.db.entity.AppUser;
import com.dglozano.escale.db.entity.Doctor;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.db.entity.PatientInfo;
import com.dglozano.escale.di.annotation.ApplicationScope;
import com.dglozano.escale.util.Constants;
import com.dglozano.escale.util.SharedPreferencesLiveData;
import com.dglozano.escale.web.EscaleRestApi;
import com.dglozano.escale.web.dto.CreatePatientDTO;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@ApplicationScope
public class DoctorRepository {

    private final PatientDao mPatientDao;
    private final DoctorDao mDoctorDao;
    private final PatientInfoDao mPatientInfoDao;
    private final UserDao mUserDao;
    private final EscaleRestApi mEscaleRestApi;
    private final SharedPreferences mSharedPreferences;
    private LiveData<Long> mLoggedDoctorId;


    @Inject
    public DoctorRepository(PatientDao patientDao,
                            PatientInfoDao patientInfoDao,
                            EscaleRestApi escaleRestApi,
                            DoctorDao doctorDao,
                            UserDao userDao,
                            SharedPreferences sharedPreferences) {
        mUserDao = userDao;
        mDoctorDao = doctorDao;
        mPatientInfoDao = patientInfoDao;
        mPatientDao = patientDao;
        mEscaleRestApi = escaleRestApi;
        mSharedPreferences = sharedPreferences;

        mLoggedDoctorId = new SharedPreferencesLiveData.SharedPreferenceLongLiveData(mSharedPreferences,
                Constants.LOGGED_DOCTOR_ID_SHARED_PREF, -1L);

    }

    public Single<Long> refreshDoctor(final Long doctorId) {
        return mEscaleRestApi.getDoctorById(doctorId)
                .map(doctorDto -> {
                    Timber.d("Retrieved doctor with id %s from Api.", doctorId);
                    Doctor doctor = new Doctor(doctorDto, Calendar.getInstance().getTime());
                    AppUser user = new AppUser(doctor);
                    mUserDao.upsert(user);
                    upsert(doctor);
                    Timber.d("Saving doctor with id %s ", doctor.getId());
                    return doctor.getId();
                })
                .subscribeOn(Schedulers.io());
    }


    public LiveData<Doctor> getDoctorById(Long doctorId) {
        return mDoctorDao.getDoctorByIdAsLiveData(doctorId);
    }

    public void upsert(Doctor doctor) {
        mDoctorDao.upsert(doctor);
    }


    public Long getLoggedDoctorId() {
        return mSharedPreferences.getLong(Constants.LOGGED_DOCTOR_ID_SHARED_PREF, -1L);
    }

    public LiveData<Long> getLoggedDoctorIdAsLiveData() {
        return mLoggedDoctorId;
    }

    public LiveData<List<PatientInfo>> getAllPatientInfoForLoggedDoctor() {
        return mPatientInfoDao.getAllPatientInfoForDoctor(getLoggedDoctorId());
    }

    public Completable addPatient(String firstName,
                                  String lastName,
                                  String email,
                                  Date birthday,
                                  int heightInCm,
                                  int genre,
                                  int phActivity) {
        CreatePatientDTO createPatientDTO =
                new CreatePatientDTO(firstName, lastName, email, birthday,
                        heightInCm, genre, phActivity, getLoggedDoctorId());
        return mEscaleRestApi.createPatientForDoctor(createPatientDTO, getLoggedDoctorId())
                .map(patientDTO -> new Patient(patientDTO, Calendar.getInstance().getTime()))
                .flatMapCompletable(patient -> {
                    AppUser user = new AppUser(patient);
                    mUserDao.upsert(user);
                    mPatientDao.upsert(patient);
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.io());
    }
}
