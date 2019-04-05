package com.dglozano.escale.ui.main.diet.all;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dglozano.escale.R;
import com.dglozano.escale.db.entity.Diet;
import com.dglozano.escale.exception.DietDownloadStateException;
import com.dglozano.escale.repository.DietRepository;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.util.ui.Event;

import java.io.File;
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
    private final MutableLiveData<Boolean> mIsRefreshingDietsList;
    private final MutableLiveData<Event<Integer>> mErrorEvent;
    private final MutableLiveData<Event<File>> mShowPdfEvent;
    private CompositeDisposable disposables;

    @Inject
    public AllDietsViewModel(PatientRepository patientRepository,
                             DietRepository dietRepository
    ) {
        disposables = new CompositeDisposable();
        mPatientRepository = patientRepository;
        mDietRepository = dietRepository;
        mIsRefreshingDietsList = new MutableLiveData<>();
        mErrorEvent = new MutableLiveData<>();
        mShowPdfEvent = new MutableLiveData<>();
        mIsRefreshingDietsList.postValue(false);
        mPatientDiets = mDietRepository.getDietsOfPatientWithId(mPatientRepository.getLoggedPatiendId());
    }

    public LiveData<Event<Integer>> getErrorEvent() {
        return mErrorEvent;
    }

    public LiveData<Event<File>> getShowPdfEvent() {
        return mShowPdfEvent;
    }

    public LiveData<List<Diet>> getDietsOfLoggedPatient() {
        return mPatientDiets;
    }

    public LiveData<Boolean> getRefreshingListStatus() {
        return mIsRefreshingDietsList;
    }

    public void refreshDiets() {
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
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }

    public void updateDiet(Diet diet) {
        mDietRepository.updateDiet(diet);
    }

    public void openOldDietFile(Diet diet) {
        try {
            File pdf = mDietRepository.getDietPdfFile(diet);
            mShowPdfEvent.postValue(new Event<>(pdf));
        } catch (DietDownloadStateException e) {
            mErrorEvent.postValue(new Event<>(R.string.error_diet_not_downloaded_yet));
        }
    }

    public void deleteDownload(Diet diet) {
        try {
            mDietRepository.deleteDownload(diet);
        } catch (DietDownloadStateException e) {
            Timber.e(e);
            mErrorEvent.postValue(new Event<>(R.string.error_diet_delete_generic));
        }
    }
}
