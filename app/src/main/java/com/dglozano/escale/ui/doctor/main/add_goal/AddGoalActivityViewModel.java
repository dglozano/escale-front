package com.dglozano.escale.ui.doctor.main.add_goal;

import com.dglozano.escale.repository.DoctorRepository;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.util.ui.Event;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class AddGoalActivityViewModel extends ViewModel {

    private final PatientRepository mPatientRepository;
    private final DoctorRepository mDoctorRepository;
    private final CompositeDisposable disposables;
    private final MutableLiveData<Event<Integer>> mErrorEvent;
    private final MutableLiveData<Event<Integer>> mSuccessEvent;
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

    public void hitChangeGoal() {
        Timber.d("hitchangegoal");
//        disposables.add(mDoctorRepository.changeGoal(mPatientRepository.getLoggedPatientId())
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .doOnSubscribe((d) -> mLoading.postValue(true))
//                .subscribe(
//                        () -> {
//                            mLoading.postValue(false);
//                        },
//                        onError -> {
//                            Timber.e(onError, "Error changing goal");
//                            mLoading.postValue(false);
//                            mErrorEvent.postValue(new Event<>(R.string.change_goal_error_msg));
//                        }
//                )
//        );
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