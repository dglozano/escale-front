package com.dglozano.escale.ui.main.home;

import com.dglozano.escale.R;
import com.dglozano.escale.repository.BodyMeasurementRepository;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.util.ui.Event;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.dglozano.escale.util.ValidationHelper.isValidBmi;
import static com.dglozano.escale.util.ValidationHelper.isValidPercentage;
import static com.dglozano.escale.util.ValidationHelper.isValidWeight;

public class AddMeasurementViewModel extends ViewModel {

    private PatientRepository mPatientRepository;
    private BodyMeasurementRepository mMeasurementRepository;
    private final CompositeDisposable disposables;
    private final MutableLiveData<Event<Integer>> mErrorEvent;
    private final MutableLiveData<Event<Boolean>> mSuccessEvent;
    private final MutableLiveData<Boolean> mLoading;

    @Inject
    public AddMeasurementViewModel(PatientRepository patientRepository,
                                   BodyMeasurementRepository measurementRepository) {
        mPatientRepository = patientRepository;
        mMeasurementRepository = measurementRepository;
        mLoading = new MutableLiveData<>();
        mErrorEvent = new MutableLiveData<>();
        mSuccessEvent = new MutableLiveData<>();
        disposables = new CompositeDisposable();
        mLoading.postValue(false);
    }

    private boolean isInputValid(String weightStr, String waterStr, String fatStr, String bonesStr,
                                 String bmiStr, String muscleStr) {
        return isValidPercentage(waterStr) && isValidPercentage(fatStr) && isValidPercentage(muscleStr) && isValidWeight(weightStr) && isValidBmi(bmiStr);
    }

    public LiveData<Boolean> getLoading() {
        return mLoading;
    }

    public LiveData<Event<Integer>> getErrorEvent() {
        return mErrorEvent;
    }

    public LiveData<Event<Boolean>> getSuccessEvent() {
        return mSuccessEvent;
    }

    public void hitAddMeasurement(String weight, String water, String fat, String bmi, String bones, String muscle) {
        if (isInputValid(weight, water, fat, bones, bmi, muscle)) {
            disposables.add(mMeasurementRepository.addMeasurement(Float.parseFloat(weight),
                    Float.parseFloat(water),
                    Float.parseFloat(fat),
                    Float.parseFloat(bmi),
                    Float.parseFloat(bones),
                    Float.parseFloat(muscle),
                    true)
                    .flatMapCompletable(id -> mPatientRepository.getUpdatedForecastFromApi(mPatientRepository.getLoggedPatientId()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe((d) -> mLoading.postValue(true))
                    .subscribe(
                            () -> {
                                Timber.d("Inserted body measurement in server and updated forecast");
                                mSuccessEvent.postValue(new Event<>(true));
                                mLoading.postValue(false);
                            },
                            error -> {
                                mLoading.postValue(false);
                                mErrorEvent.postValue(new Event<>(R.string.add_measurement_error_snackbar));
                            }
                    )
            );
        } else {
            mErrorEvent.postValue(new Event<>(R.string.input_validation_error_snackbar));
        }
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }
}
