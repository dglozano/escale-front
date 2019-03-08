package com.dglozano.escale.ui.main.stats;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.util.LongSparseArray;

import com.dglozano.escale.R;
import com.dglozano.escale.db.entity.Chat;
import com.dglozano.escale.db.entity.ChatMessage;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.repository.BodyMeasurementRepository;
import com.dglozano.escale.repository.ChatRepository;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.ui.Event;
import com.dglozano.escale.ui.main.messages.AuthorImpl;
import com.dglozano.escale.ui.main.messages.MessageImpl;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class StatsViewModel extends ViewModel {

    private BodyMeasurementRepository mMeasurementsRepository;
    private CompositeDisposable disposables;
    private final MutableLiveData<Event<Integer>> mErrorEvent;

    @Inject
    public StatsViewModel(BodyMeasurementRepository bodyMeasurementRepository) {
        mMeasurementsRepository = bodyMeasurementRepository;
        disposables = new CompositeDisposable();
        mErrorEvent = new MutableLiveData<>();
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }

    public LiveData<Event<Integer>> getErrorEvent() {
        return mErrorEvent;
    }

}