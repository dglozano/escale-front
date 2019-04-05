package com.dglozano.escale.ui.main.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dglozano.escale.R;
import com.dglozano.escale.repository.BodyMeasurementRepository;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.util.ui.Event;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

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

    private boolean isInputValid(String weight, String water, String fat, String bones,
                                 String bmi, String muscle) {
        String[] input = new String[]{weight, water, fat, bones, bmi, muscle};
        List<Float> inputFloat = Arrays.stream(input)
                .filter(s -> s != null && !s.isEmpty())
                .map(Float::parseFloat)
                .filter(f -> f >= 0f)
                .collect(Collectors.toList());
        if(inputFloat.size() < input.length)
            return false;
        if(inputFloat.get(1) > 100f || inputFloat.get(2) > 100f || inputFloat.get(5) > 100f) {
            return false;
        }
        return true;
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

    public void hitAddMeasurement(String weight, String water, String fat, String bmi,  String bones, String muscle) {
        if (isInputValid(weight, water, fat, bones, bmi, muscle)) {
            disposables.add(mMeasurementRepository.addMeasurement(Float.parseFloat(weight),
                    Float.parseFloat(water),
                    Float.parseFloat(fat),
                    Float.parseFloat(bmi),
                    Float.parseFloat(bones),
                    Float.parseFloat(muscle),
                    true)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe((d) -> mLoading.postValue(true))
                    .subscribe(
                            insertedId -> {
                                Timber.d("Inserted body measurement in server with id %s", insertedId);
                                mSuccessEvent.postValue(new Event<>(true));
                                mLoading.postValue(false);
                            },
                            onError -> {
                                mLoading.postValue(false);
                                mErrorEvent.postValue(new Event<>(R.string.add_measurement_error_snackbar));
                            }
                    )
            );
        } else {
            mErrorEvent.postValue(new Event<>(R.string.add_measurement_input_error_snackbar));
        }
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }
}
