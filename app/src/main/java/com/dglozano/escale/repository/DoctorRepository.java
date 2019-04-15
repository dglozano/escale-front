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

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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

    public Completable refreshDoctor(final Long doctorId) {
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
                .flatMapCompletable(this::refreshPatientInfoForDoctor)
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

    public Completable refreshPatientInfoForDoctor(Long doctorId) {
        return mEscaleRestApi.getAllPatientsInfoOfDoctor(doctorId)
                .flatMapCompletable(patientInfos -> {
                    patientInfos.forEach(patientInfo -> {
                        mPatientInfoDao.upsert(patientInfo);
                        mPatientDao.getUserByIdOptional(patientInfo.getPatientId()).ifPresent(patient -> Timber.d("patient %s", patient.getId()));
                        Patient patientWithNoDataYet = new Patient(patientInfo.getPatientId(), patientInfo.getDoctorId(), Calendar.getInstance().getTime());
                        mUserDao.upsert(new AppUser(patientWithNoDataYet.getId(), patientWithNoDataYet.getLastUpdate()));
                        mPatientDao.upsert(patientWithNoDataYet);
                    });
                    return Completable.complete();
                });
    }

    public Completable addAlertToPatientInfo(Long patient_id, Long doctor_id) {
        if (getLoggedDoctorId() != -1) {
            return mPatientInfoDao.getPatientInfoByPatientIdOfDoctor(doctor_id, patient_id)
                    .flatMapCompletable(patientInfoOptional -> {
                        patientInfoOptional.ifPresent(patientInfo -> {
                            patientInfo.setAlerts(patientInfo.getAlerts() + 1);
                            mPatientInfoDao.upsert(patientInfo);
                        });
                        return Completable.complete();
                    });
        }
        return Completable.complete();
    }

    public Completable addMessageToPatientInfo(Long sender_id) {
        if (getLoggedDoctorId() != -1 && getLoggedDoctorId() != sender_id.longValue()) {
            return mPatientInfoDao.getPatientInfoByPatientIdOfDoctor(getLoggedDoctorId(), sender_id)
                    .flatMapCompletable(patientInfoOptional -> {
                        patientInfoOptional.ifPresent(patientInfo -> {
                            patientInfo.setMessages(patientInfo.getMessages() + 1);
                            mPatientInfoDao.upsert(patientInfo);
                        });
                        return Completable.complete();
                    });
        }
        return Completable.complete();
    }

    public Single<Optional<Doctor>> getLoggedDoctorSingle() {
        return mDoctorDao.getDoctorByIdSingle(getLoggedDoctorId());
    }

    public Completable uploadDiet(File diet, String mediaType, String filename, Long patientId) {
        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(MediaType.parse(mediaType), diet);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", diet.getName(), requestFile);

        // finally, execute the request
        return mEscaleRestApi.uploadDiet(body, patientId, filename)
                .doOnComplete(() -> {
                    Timber.d("Was temp diet deleted? %s", diet.delete());
                })
                .subscribeOn(Schedulers.io());
    }
}
