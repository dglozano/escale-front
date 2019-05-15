package com.dglozano.escale.repository;

import android.content.SharedPreferences;

import com.dglozano.escale.db.dao.ForecastDao;
import com.dglozano.escale.db.dao.PatientDao;
import com.dglozano.escale.db.dao.UserDao;
import com.dglozano.escale.db.entity.AppUser;
import com.dglozano.escale.db.entity.Doctor;
import com.dglozano.escale.db.entity.MeasurementForecast;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.di.annotation.ApplicationScope;
import com.dglozano.escale.di.annotation.BaseUrl;
import com.dglozano.escale.exception.AccountDisabledException;
import com.dglozano.escale.util.AppExecutors;
import com.dglozano.escale.util.Constants;
import com.dglozano.escale.util.SharedPreferencesLiveData;
import com.dglozano.escale.web.EscaleRestApi;
import com.dglozano.escale.web.dto.UpdatePatientDTO;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

import javax.annotation.Nullable;
import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import timber.log.Timber;

import static com.dglozano.escale.util.Constants.FRESH_TIMEOUT;

@ApplicationScope
public class PatientRepository {

    private PatientDao mPatientDao;
    private ForecastDao mForecastDao;
    private DoctorRepository mDoctorRepository;
    private AlertRepository mAlertRepository;
    private UserDao mUserDao;
    private EscaleRestApi mEscaleRestApi;
    private SharedPreferences mSharedPreferences;
    private LiveData<Long> mLoggedUserId;
    private AppExecutors appExecutors;
    private MediatorLiveData<Optional<MeasurementForecast>> mLastForecastWithPredictions;
    private String baseUrl;

    @Inject
    public PatientRepository(PatientDao patientDao,
                             EscaleRestApi escaleRestApi,
                             DoctorRepository doctorRepository,
                             AlertRepository alertRepository,
                             UserDao userDao,
                             ForecastDao forecastDao,
                             AppExecutors appExecutors,
                             SharedPreferences sharedPreferences,
                             @BaseUrl String baseUrl) {
        this.baseUrl = baseUrl;
        this.mForecastDao = forecastDao;
        mUserDao = userDao;
        mAlertRepository = alertRepository;
        mDoctorRepository = doctorRepository;
        this.appExecutors = appExecutors;
        mPatientDao = patientDao;
        mEscaleRestApi = escaleRestApi;
        mSharedPreferences = sharedPreferences;
        mLoggedUserId = new SharedPreferencesLiveData.SharedPreferenceLongLiveData(mSharedPreferences,
                Constants.LOGGED_USER_ID_SHARED_PREF, -1L);

        mLastForecastWithPredictions = new MediatorLiveData<>();
        mLastForecastWithPredictions.addSource(Transformations.switchMap(mLoggedUserId, id -> mForecastDao.getLastForecastOfUserWithIdAsLiveData(getLoggedPatientId())),
                mfOpt -> {
                    if (mfOpt.isPresent()) {
                        this.appExecutors.getDiskIO().execute(() -> {
                            mLastForecastWithPredictions.postValue(mForecastDao.getForecastWithPredictions(mfOpt.get().getId()));
                        });
                    } else {
                        mLastForecastWithPredictions.postValue(Optional.empty());
                    }
                });
    }

    @Nullable
    public LiveData<Patient> getPatientById(Long userId) {
        return mPatientDao.getPatientById(userId);
    }

    public Long getLoggedPatientId() {
        return mSharedPreferences.getLong(Constants.LOGGED_USER_ID_SHARED_PREF, -1L);
    }

    public void setLoggedPatientId(Long patientId) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putLong(Constants.LOGGED_USER_ID_SHARED_PREF, patientId);
        editor.apply();
    }

    public LiveData<Patient> getLoggedPatient() {
        return mPatientDao.getPatientById(getLoggedPatientId());
    }

    public Single<Patient> getLoggedPatientSingle() {
        return mPatientDao.getPatientSingleById(getLoggedPatientId());
    }

    public LiveData<Long> getLoggedPatientIdAsLiveData() {
        return mLoggedUserId;
    }

    public Single<Long> refreshPatient(final Long userId) {
        return mPatientDao.hasUser(userId, FRESH_TIMEOUT)
                .map(freshInt -> freshInt == 1)
                .flatMapMaybe(hasFreshUser -> {
                    // TODO: For now, I will query for the patient information always, even if it is fresh.
//                    if (!hasFreshUser) {
//                        return Maybe.fromSingle(mEscaleRestApi.getPatientById(userId));
//                    } else {
//                        return Maybe.empty();
//                    }
                    return Maybe.fromSingle(mEscaleRestApi.getPatientById(userId));
                })
                .flatMapSingle(patientDTO -> {
                    Timber.d("Retrieved user with id %s from Api.", userId);
                    if (!patientDTO.isEnabled()) {
                        throw new AccountDisabledException();
                    }
                    return Single.fromCallable(() -> {
                        Doctor doctor = new Doctor(patientDTO.getDoctorDTO(), Calendar.getInstance().getTime());
                        AppUser user = new AppUser(doctor);
                        mUserDao.upsert(user);
                        mDoctorRepository.upsert(doctor);
                        Timber.d("Saving doctor with id %s ", doctor.getId());
                        return patientDTO;
                    });
                })
                .flatMap(patientDTO -> Single.fromCallable(() -> {
                    Patient patient = new Patient(patientDTO, Calendar.getInstance().getTime());
                    AppUser user = new AppUser(patient);
                    mUserDao.upsert(user);
                    mPatientDao.upsert(patient);
                    return patient;
                }))
                .flatMap(patient ->
                        Completable.mergeArray(getUpdatedForecastFromApi(patient), mAlertRepository.refreshAlertsOfPatient(patient.getId()))
                                .andThen(Single.just(patient.getId()))
                );
    }

    public Completable saveNewGoalOnNotified(Long loggedPatiendId, Float weightInKg, String dueDateStr, String startDateStr) {
        return mPatientDao.getPatientSingleById(loggedPatiendId)
                .flatMapCompletable(patient -> Completable.fromCallable(() -> {
                    String format = "yyyy-MM-dd'T'HH:mm:ss.SSS";
                    SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                    Date dueDate = sdf.parse(dueDateStr);
                    Date startDate = sdf.parse(startDateStr);
                    patient.setGoalInKg(weightInKg);
                    patient.setGoalDueDate(dueDate);
                    patient.setGoalStartDate(startDate);

                    mPatientDao.upsert(patient);
                    return Completable.complete();
                }));
    }

    public Completable uploadPicture(File picture, String mediaType) {
        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(MediaType.parse(mediaType),
                        picture
                );

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", picture.getName(), requestFile);

        // finally, execute the request
        return mEscaleRestApi.uploadProfilePicture(body, getLoggedPatientId())
                .doOnComplete(() -> {
                    Timber.d("Was temp picture deleted? %s", picture.delete());
                });
    }

    public URL getProfileImageUrlOfLoggedPatient() throws MalformedURLException {
        return getProfileImageUrlOfPatient(getLoggedPatientId());
    }

    public URL getProfileImageUrlOfPatient(Long id) throws MalformedURLException {
        return new URL(String.format("%s/api/patients/%s/profile_image", baseUrl, id));
    }

    public Completable updateLoggedPatientHeightAndActivity(int newHeight, int newActivity, Long loggedPatient) {
        UpdatePatientDTO updatePatientDTO = new UpdatePatientDTO(newHeight, newActivity);
        return mEscaleRestApi.updatePatientWithId(updatePatientDTO, getLoggedPatientId())
                .andThen(mPatientDao.getPatientSingleById(loggedPatient))
                .flatMapCompletable(patient -> {
                    patient.setHeightInCm(newHeight);
                    patient.setPhysicalActivity(newActivity);
                    patient.setHasToUpdateDataInScale(true);
                    mPatientDao.upsert(patient);
                    return Completable.complete();
                });
    }

    public Single<Long> setHasToUpdateDataInScale(boolean flag, Long patientId) {
        return mPatientDao.getPatientSingleById(patientId)
                .flatMap(patient -> Single.fromCallable(() -> {
                    patient.setHasToUpdateDataInScale(flag);
                    mPatientDao.upsert(patient);
                    return patient.getId();
                }));
    }

    public Optional<Float> getGoalOfPatientWithId(Long loggedPatiendId) {
        return mPatientDao.getGoalOfPatient(loggedPatiendId);
    }

    public Completable getUpdatedForecastFromApi(Patient patient) {
        return mEscaleRestApi.getMeasurementForecastOfUser(patient.getId(), Constants.FORECAST_AMOUNT, false)
                .flatMap(measurementForecastResponse -> {
                    if (measurementForecastResponse.code() == 200) {
                        mForecastDao.deleteAllByUserId(patient.getId());
                        mForecastDao.upsertForecastWithPredictions(measurementForecastResponse.body());
                    }
                    return Single.just(patient);
                }).flatMapCompletable(p -> Completable.complete())
                .subscribeOn(Schedulers.io());
    }

    public Completable getUpdatedForecastFromApi(Long loggedPatientId) {
        return mEscaleRestApi.getMeasurementForecastOfUser(loggedPatientId, Constants.FORECAST_AMOUNT, false)
                .zipWith(mPatientDao.getPatientSingleById(loggedPatientId), (measurementForecastResponse, patient) -> {
                    if (measurementForecastResponse.code() == 200) {
                        mForecastDao.deleteAllByUserId(loggedPatientId);
                        mForecastDao.upsertForecastWithPredictions(measurementForecastResponse.body());
                    }
                    return patient;
                }).flatMapCompletable(p -> Completable.complete())
                .subscribeOn(Schedulers.io());
    }

    public LiveData<Optional<MeasurementForecast>> getMeasurementForecastOfLoggedPatient() {
        return mLastForecastWithPredictions;
    }
}
