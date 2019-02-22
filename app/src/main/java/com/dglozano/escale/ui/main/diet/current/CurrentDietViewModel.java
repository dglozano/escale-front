package com.dglozano.escale.ui.main.diet.current;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.dglozano.escale.db.entity.Diet;
import com.dglozano.escale.repository.DietRepository;
import com.dglozano.escale.repository.PatientRepository;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class CurrentDietViewModel extends ViewModel {

    private PatientRepository mPatientRepository;
    private DietRepository mDietRepository;
    private LiveData<Diet> mCurrentDiet;
    private MutableLiveData<Boolean> mIsRefreshingDietsList;
    private final CompositeDisposable disposables;


    @Inject
    public CurrentDietViewModel(PatientRepository patientRepository,
                                DietRepository dietRepository
    ) {
        disposables = new CompositeDisposable();
        mPatientRepository = patientRepository;
        mDietRepository = dietRepository;
        mIsRefreshingDietsList = new MutableLiveData<>();
        mIsRefreshingDietsList.postValue(false);
        mCurrentDiet = new MutableLiveData<>();
    }

    public LiveData<Boolean> getRefreshingListStatus() {
        return mIsRefreshingDietsList;
    }

    public void refreshCurrentDiet() {
        //TODO

//        disposables.add(mDietRepository.refreshDietsCompletable(mPatientRepository.getLoggedPatiendId())
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .doOnSubscribe(d -> mIsRefreshingDietsList.postValue(true))
//                .subscribe(() -> mIsRefreshingDietsList.postValue(false),
//                        error -> {
//                            Timber.e(error, "Error refreshing diets");
//                            mIsRefreshingDietsList.postValue(false);
//                        })
//        );
        this.mIsRefreshingDietsList.postValue(false);
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }

    public LiveData<Diet> getCurrentDiet() {
        //TODO
        return mCurrentDiet;
    }
}
