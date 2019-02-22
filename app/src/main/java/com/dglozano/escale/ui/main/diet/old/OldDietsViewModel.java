package com.dglozano.escale.ui.main.diet.old;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.dglozano.escale.R;
import com.dglozano.escale.db.entity.Diet;
import com.dglozano.escale.exception.DietDownloadStateException;
import com.dglozano.escale.repository.DietRepository;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.ui.Event;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class OldDietsViewModel extends ViewModel {

    private PatientRepository mPatientRepository;
    private DietRepository mDietRepository;
    private LiveData<List<Diet>> mPatientDiets;
    private final MutableLiveData<Boolean> mIsRefreshingDietsList;
    private final MutableLiveData<Event<Integer>> mErrorEvent;
    private final MutableLiveData<Event<File>> mShowPdfEvent;
    private CompositeDisposable disposables;

    @Inject
    public OldDietsViewModel(PatientRepository patientRepository,
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

    public void openOldDietFile(Diet diet) {
        Timber.d("Diet %s clicked", diet.getFileName());
        try {
            File pdf = mDietRepository.getDietPdfFile(diet);
            Timber.d("Posting show pdf event for file %s", pdf.getAbsolutePath());
            mShowPdfEvent.postValue(new Event<>(pdf));
        } catch (DietDownloadStateException e) {
            mErrorEvent.postValue(new Event<>(R.string.error_diet_download_generic));
        }
    }

    public void startDownload(Diet diet) {
        try {
            disposables.add(mDietRepository.download(diet)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> Timber.d("Completed download of diet %s", diet.getFileName()),
                            onError -> Timber.e(onError, "Error downloading diet %s", diet.getFileName())));
        } catch (DietDownloadStateException e) {
            Timber.e(e);
            mErrorEvent.postValue(new Event<>(R.string.error_diet_download_generic));
        }
    }

    public void cancelDownload(Diet diet) {
        try {
            mDietRepository.cancelDownload(diet);
        } catch (DietDownloadStateException e) {
            Timber.e(e);
            mErrorEvent.postValue(new Event<>(R.string.error_diet_cancel_download_generic));
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
