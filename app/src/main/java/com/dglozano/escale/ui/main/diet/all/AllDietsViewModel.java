package com.dglozano.escale.ui.main.diet.all;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.dglozano.escale.db.entity.Diet;
import com.dglozano.escale.repository.DietRepository;
import com.dglozano.escale.repository.PatientRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class AllDietsViewModel extends ViewModel {

    private PatientRepository mPatientRepository;
    private DietRepository mDietRepository;
    private LiveData<List<Diet>> mPatientDiets;
    private MutableLiveData<Boolean> mIsRefreshingDietsList;
    private final CompositeDisposable disposables;


    @Inject
    public AllDietsViewModel(PatientRepository patientRepository,
                             DietRepository dietRepository
    ) {
        disposables = new CompositeDisposable();
        mPatientRepository = patientRepository;
        mDietRepository = dietRepository;
        mIsRefreshingDietsList = new MutableLiveData<>();
        mIsRefreshingDietsList.postValue(false);
        mPatientDiets = mDietRepository.getDietsOfPatientWithId(mPatientRepository.getLoggedPatiendId());
    }

    public LiveData<List<Diet>> getDietsOfLoggedPatient() {
        return mPatientDiets;
    }

    public LiveData<Boolean> getRefreshingListStatus() {
        return mIsRefreshingDietsList;
    }

    public void refreshDiets() {
        disposables.add(mDietRepository.refreshDietsCompletable(mPatientRepository.getLoggedPatiendId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(d -> mIsRefreshingDietsList.postValue(true))
                .subscribe(() -> mIsRefreshingDietsList.postValue(false),
                        error -> {
                            Timber.e(error, "Error refreshing diets");
                            mIsRefreshingDietsList.postValue(false);
                        })
        );
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }
}
