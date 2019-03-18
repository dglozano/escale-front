package com.dglozano.escale.ui.main.diet.current;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.dglozano.escale.R;
import com.dglozano.escale.db.entity.Diet;
import com.dglozano.escale.exception.DietDownloadStateException;
import com.dglozano.escale.repository.DietRepository;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.util.ui.Event;

import java.io.File;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class CurrentDietViewModel extends ViewModel {

    private PatientRepository mPatientRepository;
    private DietRepository mDietRepository;
    private MutableLiveData<Boolean> mIsRefreshingDietsList;
    private final MutableLiveData<Event<Integer>> mErrorEvent;
    private final LiveData<Diet> mCurrentDiet;

    private final CompositeDisposable disposables;


    @Inject
    public CurrentDietViewModel(PatientRepository patientRepository,
                                DietRepository dietRepository) {
        disposables = new CompositeDisposable();
        mPatientRepository = patientRepository;
        mDietRepository = dietRepository;
        mCurrentDiet = mDietRepository.getCurrentDiet(mPatientRepository.getLoggedPatiendId());
        mIsRefreshingDietsList = new MutableLiveData<>();
        mIsRefreshingDietsList.postValue(false);
        mErrorEvent = new MutableLiveData<>();

    }

    public LiveData<Event<Integer>> getErrorEvent() {
        return mErrorEvent;
    }


    public LiveData<Boolean> getRefreshingListStatus() {
        return mIsRefreshingDietsList;
    }

    public void refreshCurrentDiet() {
        disposables.add(mDietRepository.refreshDietsSingle(mPatientRepository.getLoggedPatiendId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(d -> mIsRefreshingDietsList.postValue(true))
                .subscribe((newDiets) -> mIsRefreshingDietsList.postValue(false),
                        error -> {
                            Timber.e(error, "Error refreshing diets");
                            mIsRefreshingDietsList.postValue(false);
                        })
        );
        this.mIsRefreshingDietsList.postValue(false);
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }

    public File getDietFile(Diet diet) {
        File file = null;
        try {
            file = mDietRepository.getDietPdfFile(diet);
        } catch (DietDownloadStateException e) {
            Timber.e(e);
            mErrorEvent.postValue(new Event<>(R.string.error_diet_not_downloaded_yet));
        }
        return file;
    }

    public void updateDiet(Diet diet) {
        mDietRepository.updateDiet(diet);
    }


    public LiveData<Diet> getCurrentDiet() {
        return mCurrentDiet;
    }
}
