package com.dglozano.escale.ui.doctor.main;

import android.net.Uri;

import com.dglozano.escale.R;
import com.dglozano.escale.repository.DoctorRepository;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.util.ui.Event;

import java.io.File;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class AddDietActivityViewModel extends ViewModel {

    private final PatientRepository mPatientRepository;
    private final DoctorRepository mDoctorRepository;
    private final CompositeDisposable disposables;
    private final MutableLiveData<Event<Integer>> mErrorEvent;
    private final MutableLiveData<Event<Integer>> mSuccessEvent;
    private final MutableLiveData<Boolean> mLoading;
    private Uri dietFileUri;

    @Inject
    public AddDietActivityViewModel(DoctorRepository doctorRepository, PatientRepository patientRepository) {
        mLoading = new MutableLiveData<>();
        mErrorEvent = new MutableLiveData<>();
        mSuccessEvent = new MutableLiveData<>();
        disposables = new CompositeDisposable();
        mDoctorRepository = doctorRepository;
        mPatientRepository = patientRepository;
    }

    public void hitUploadDiet(File picture, String mediaType, String filename) {
        disposables.add(mDoctorRepository.uploadDiet(picture, mediaType, filename, mPatientRepository.getLoggedPatientId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe((d) -> mLoading.postValue(true))
                .subscribe(
                        () -> {
                            mSuccessEvent.postValue(new Event<>(R.string.upload_diet_success_msg));
                            mLoading.postValue(false);
                        },
                        onError -> {
                            Timber.e(onError, "Error uploading diet");
                            mLoading.postValue(false);
                            mErrorEvent.postValue(new Event<>(R.string.upload_diet_error_msg));
                        }
                )
        );
    }

    public Uri getDietFileUri() {
        return dietFileUri;
    }

    public void setDietFileUri(Uri dietFileUri) {
        this.dietFileUri = dietFileUri;
    }

    public LiveData<Event<Integer>> getErrorEvent() {
        return mErrorEvent;
    }

    public LiveData<Event<Integer>> getSuccessEvent() {
        return mSuccessEvent;
    }

    public LiveData<Boolean> getLoading() {
        return mLoading;
    }

}