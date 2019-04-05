package com.dglozano.escale.ui.drawer.profile;

import com.dglozano.escale.R;
import com.dglozano.escale.db.entity.Doctor;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.repository.DoctorRepository;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.util.AbsentLiveData;
import com.dglozano.escale.util.ui.Event;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.Nullable;
import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class PatientProfileActivityViewModel extends ViewModel {

    private final CompositeDisposable disposables;
    private final LiveData<Patient> mLoggedPatient;
    private final LiveData<Doctor> mLoggedDoctor;
    private final MutableLiveData<Event<Integer>> mErrorEvent;
    private final MutableLiveData<Event<Integer>> mSuccessEvent;
    private final MutableLiveData<Boolean> mLoading;
    private PatientRepository mPatientRepository;
    private DoctorRepository mDoctorRepository;

    @Inject
    public PatientProfileActivityViewModel(PatientRepository patientRepository,
                                           DoctorRepository doctorRepository) {
        mPatientRepository = patientRepository;
        mDoctorRepository = doctorRepository;
        mLoading = new MutableLiveData<>();
        mErrorEvent = new MutableLiveData<>();
        mSuccessEvent = new MutableLiveData<>();
        disposables = new CompositeDisposable();
        mLoggedPatient = mPatientRepository.getLoggedPatient();
        mLoggedDoctor = Transformations.switchMap(mLoggedPatient, patient -> {
            if (patient != null) {
                return mDoctorRepository.getDoctorById(patient.getDoctorId());
            } else {
                return AbsentLiveData.create();
            }
        });
        mLoading.postValue(false);
    }

    public void hitUploadPicture(File picture, String mediaType) {
        disposables.add(mPatientRepository.uploadPicture(picture, mediaType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe((d) -> mLoading.postValue(true))
                .subscribe(
                        () -> {
                            mSuccessEvent.postValue(new Event<>(R.string.upload_picture_success_msg));
                            mLoading.postValue(false);
                        },
                        onError -> {
                            mLoading.postValue(false);
                            mErrorEvent.postValue(new Event<>(R.string.upload_picture_error_msg));
                        }
                )
        );
    }

    public void hitUpdateUserHeightAndActivity(int newHeight, int newActivity) {
        disposables.add(mPatientRepository
                .updateLoggedPatientHeightAndActivity(newHeight, newActivity, mPatientRepository.getLoggedPatiendId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe((d) -> mLoading.postValue(true))
                .subscribe(
                        () -> {
                            mSuccessEvent.postValue(new Event<>(R.string.update_user_data_success));
                            mLoading.postValue(false);
                        },
                        onError -> {
                            mLoading.postValue(false);
                            mErrorEvent.postValue(new Event<>(R.string.update_user_data_error_msg));
                        }
                )
        );
    }

    public LiveData<Boolean> getLoading() {
        return mLoading;
    }

    public LiveData<Event<Integer>> getErrorEvent() {
        return mErrorEvent;
    }

    public LiveData<Event<Integer>> getSuccessEvent() {
        return mSuccessEvent;
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }

    public LiveData<Patient> getLoggedPatient() {
        return mLoggedPatient;
    }

    public URL getProfileImageUrlOfLoggedPatient() throws MalformedURLException {
        return mPatientRepository.getProfileImageUrlOfLoggedPatient();
    }

    public LiveData<Doctor> getDoctorOfLoggedPatient() {
        return mLoggedDoctor;
    }

    public int getPatientHeight() {
        return mLoggedPatient.getValue() == null ? -1 : mLoggedPatient.getValue().getHeightInCm();
    }

    public int getPatientActivity() {
        return mLoggedPatient.getValue() == null ? -1 : mLoggedPatient.getValue().getPhysicalActivity();
    }
}
