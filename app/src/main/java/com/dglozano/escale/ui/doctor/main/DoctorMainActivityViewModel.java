package com.dglozano.escale.ui.doctor.main;

import com.dglozano.escale.db.entity.Doctor;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.db.entity.PatientInfo;
import com.dglozano.escale.repository.BodyMeasurementRepository;
import com.dglozano.escale.repository.ChatRepository;
import com.dglozano.escale.repository.DoctorRepository;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.repository.UserRepository;
import com.dglozano.escale.util.ui.Event;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class DoctorMainActivityViewModel extends ViewModel {

    private final PatientRepository mPatientRepository;
    private final ChatRepository mChatRepository;
    private final BodyMeasurementRepository mMeasurementRepository;
    private final DoctorRepository mDoctorRepository;
    private final UserRepository mUserRepository;
    private final CompositeDisposable disposables;

    private final MutableLiveData<Event<Integer>> mLogoutEvent;
    private final MediatorLiveData<Boolean> mLoadingStatus;
    private final LiveData<List<PatientInfo>> mPatientInfos;
    private final LiveData<Event<Boolean>> mMustChangePassword;
    private final LiveData<Doctor> mLoggedDoctor;

    @Inject
    public DoctorMainActivityViewModel(PatientRepository patientRepository,
                                       DoctorRepository doctorRepository,
                                       BodyMeasurementRepository bodyMeasurementRepository,
                                       ChatRepository chatRepository,
                                       UserRepository userRepository) {
        disposables = new CompositeDisposable();

        mPatientRepository = patientRepository;
        mChatRepository = chatRepository;
        mMeasurementRepository = bodyMeasurementRepository;
        mUserRepository = userRepository;
        mDoctorRepository = doctorRepository;
        mPatientInfos = mDoctorRepository.getAllPatientInfoForLoggedDoctor();

        mLogoutEvent = new MutableLiveData<>();
        mLoadingStatus = new MediatorLiveData<>();
        mLoadingStatus.setValue(false);
        mLoggedDoctor = mDoctorRepository.getLoggedDoctor();

        mMustChangePassword = Transformations.map(mLoggedDoctor,
                doctor -> new Event<>(doctor != null && !doctor.hasChangedDefaultPassword()));
    }

    public void refreshData() {
        disposables.add(mDoctorRepository
                .refreshDoctor(getLoggedDoctorId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(d -> mLoadingStatus.postValue(true))
                .doFinally(() -> mLoadingStatus.postValue(false))
                .subscribe(() -> {
                    Timber.d("Doctor refreshed successfully");
                    //TODO do something else?
                }, error -> {
                    Timber.e(error, "Error while refreshing doctor");
                })
        );
    }

    public void logout() {
        logout(-1);
    }

    public void logout(Integer logoutMessageResourceId) {
        mLogoutEvent.postValue(new Event<>(logoutMessageResourceId));
        mUserRepository.logout();
    }

    public LiveData<Event<Boolean>> watchMustChangePassword() {
        return mMustChangePassword;
    }

    public LiveData<Event<Integer>> getLogoutEvent() {
        return mLogoutEvent;
    }

    public LiveData<String> getFirebaseToken() {
        return mUserRepository.getFirebaseDeviceToken();
    }

    public Long getLoggedDoctorId() {
        return mDoctorRepository.getLoggedDoctorId();
    }

    public Boolean isFirebaseTokenSent() {
        return mUserRepository.isFirebaseTokenSent();
    }

    public LiveData<List<PatientInfo>> getAllPatientInfoForLoggedDoctor() {
        return mPatientInfos;
    }

    public MediatorLiveData<Boolean> getLoadingStatus() {
        return mLoadingStatus;
    }

    public URL getProfileImageUrlPatient(Long patientId) throws MalformedURLException {
        return mPatientRepository.getProfileImageUrlOfPatient(patientId);
    }

    public void setPatientId(Long patientId) {
        mPatientRepository.setLoggedPatientId(patientId);
    }

    public void handleMustChangePasswordEvent() {
        Objects.requireNonNull(mMustChangePassword.getValue()).handleContent();
    }
}




