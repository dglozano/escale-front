package com.dglozano.escale.ui.doctor.main.add_goal;

import com.dglozano.escale.R;
import com.dglozano.escale.repository.DoctorRepository;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.util.ui.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.dglozano.escale.util.ValidationHelper.isValidGoalDueDate;
import static com.dglozano.escale.util.ValidationHelper.isValidWeight;

public class AddGoalActivityViewModel extends ViewModel {

    private final PatientRepository mPatientRepository;
    private final DoctorRepository mDoctorRepository;
    private final CompositeDisposable disposables;
    private final MutableLiveData<Event<Integer>> mErrorEvent;
    private final MutableLiveData<Event<Boolean>> mSuccessEvent;
    private final MutableLiveData<Boolean> mLoading;

    @Inject
    public AddGoalActivityViewModel(DoctorRepository doctorRepository,
                                    PatientRepository patientRepository) {
        mLoading = new MutableLiveData<>();
        mErrorEvent = new MutableLiveData<>();
        mSuccessEvent = new MutableLiveData<>();
        disposables = new CompositeDisposable();
        mDoctorRepository = doctorRepository;
        mPatientRepository = patientRepository;
    }

    private boolean isInputValid(CharSequence goalInKg,
                                 CharSequence dueDate) {
        return isValidWeight(goalInKg) && isValidGoalDueDate(dueDate);
    }

    public void hitChangeGoal(CharSequence goalInKgStr, CharSequence goalDueDateStr) {
        if (isInputValid(goalInKgStr, goalDueDateStr)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date goalDueDate = sdf.parse(goalDueDateStr.toString());
                Float goalInKg = Float.parseFloat(goalInKgStr.toString());
                disposables.add(
                        mDoctorRepository.addGoal(mDoctorRepository.getLoggedDoctorId(),
                                mPatientRepository.getLoggedPatientId(), goalInKg, goalDueDate)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnSubscribe((d) -> mLoading.postValue(true))
                                .subscribe(
                                        () -> {
                                            mLoading.postValue(false);
                                            mSuccessEvent.postValue(new Event<>(true));
                                        },
                                        onError -> {
                                            Timber.e(onError, "Error changing goal");
                                            mLoading.postValue(false);
                                            mErrorEvent.postValue(new Event<>(R.string.change_goal_error_msg));
                                        }
                                )
                );
            } catch (Exception e) {
                Timber.e(e);
                mErrorEvent.postValue(new Event<>(R.string.input_validation_error_snackbar));
            }
        } else {
            mErrorEvent.postValue(new Event<>(R.string.input_validation_error_snackbar));
        }
    }

    public LiveData<Event<Integer>> getErrorEvent() {
        return mErrorEvent;
    }

    public LiveData<Event<Boolean>> getSuccessEvent() {
        return mSuccessEvent;
    }

    public LiveData<Boolean> getLoading() {
        return mLoading;
    }

}