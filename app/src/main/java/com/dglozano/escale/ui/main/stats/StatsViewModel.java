package com.dglozano.escale.ui.main.stats;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.repository.BodyMeasurementRepository;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.ui.Event;
import com.dglozano.escale.util.Constants;
import com.github.mikephil.charting.data.Entry;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

import static com.dglozano.escale.ui.main.stats.StatsViewModel.StatFilter.WEIGHT;
import static com.dglozano.escale.ui.main.stats.StatsViewModel.StatFilter.valueOf;

public class StatsViewModel extends ViewModel {

    private PatientRepository mPatientRepository;
    private BodyMeasurementRepository mMeasurementsRepository;
    private CompositeDisposable disposables;
    private final MutableLiveData<Event<Integer>> mErrorEvent;
    private final LiveData<List<BodyMeasurement>> mBodyMeasurementList;
    private final MutableLiveData<StatFilter> mSelectedStat;
    private final MutableLiveData<Boolean> mFilterExpandedState;
    private final MediatorLiveData<List<Entry>> mChartEntriesList;

    @Inject
    public StatsViewModel(BodyMeasurementRepository bodyMeasurementRepository,
                          PatientRepository patientRepository) {
        mPatientRepository = patientRepository;
        mMeasurementsRepository = bodyMeasurementRepository;
        disposables = new CompositeDisposable();
        mErrorEvent = new MutableLiveData<>();

        mFilterExpandedState = new MutableLiveData<>();
        mFilterExpandedState.postValue(true);

        mSelectedStat = new MutableLiveData<>();
        mSelectedStat.postValue(WEIGHT);

        mBodyMeasurementList = mMeasurementsRepository.getLastBodyMeasurementsOfUserWithId(
                patientRepository.getLoggedPatiendId(),
                Constants.BODY_MEASUREMENTS_DEFAULT_LIMIT);

        mChartEntriesList = new MediatorLiveData<>();
        mChartEntriesList.addSource(mBodyMeasurementList, newList -> {
            mChartEntriesList.postValue(getEntriesListFromMeasurement(newList, getSelectedStat()));
        });
        mChartEntriesList.addSource(getSelectedStatAsLiveData(), statFilter -> {
            mChartEntriesList.postValue(getEntriesListFromMeasurement(mBodyMeasurementList.getValue(), statFilter == null ? WEIGHT : statFilter));
        });
    }

    private List<Entry> getEntriesListFromMeasurement(List<BodyMeasurement> bodyMeasurements, StatFilter selectedFilter) {
        List<Entry> list = bodyMeasurements == null ? Collections.emptyList() :
                bodyMeasurements.stream()
                        .map(bodyMeasurement -> {
                            float yData = bodyMeasurement.getWeight();
                            switch (selectedFilter) {
                                case MUSCLE:
                                    yData = bodyMeasurement.getMuscles();
                                    break;
                                case WATER:
                                    yData = bodyMeasurement.getWater();
                                    break;
                                case BONES:
                                    yData = bodyMeasurement.getBones();
                                    break;
                                case IMC:
                                    yData = bodyMeasurement.getBmi();
                                    break;
                                case FAT:
                                    yData = bodyMeasurement.getFat();
                                    break;
                            }
                            return new Entry(bodyMeasurement.getDate().getTime(), yData);
                        })
                        .collect(Collectors.toList());
        Collections.reverse(list);
        return list;
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }

    public LiveData<Event<Integer>> getErrorEvent() {
        return mErrorEvent;
    }

    public LiveData<List<Entry>> getChartEntries() {
        return mChartEntriesList;
    }

    public LiveData<Boolean> getFilterExpansionState() {
        return mFilterExpandedState;
    }

    public void setSelectedStat(int position) {
        mSelectedStat.postValue(valueOf(position));
    }

    public LiveData<StatFilter> getSelectedStatAsLiveData() {
        return mSelectedStat;
    }

    public StatFilter getSelectedStat() {
        return mSelectedStat.getValue() == null ? StatFilter.WEIGHT : mSelectedStat.getValue();
    }

    public void toggleFiltersExpansion() {
        mFilterExpandedState.postValue(mFilterExpandedState.getValue() != null && !mFilterExpandedState.getValue());
    }

    protected enum StatFilter {
        WEIGHT(0),
        WATER(1),
        FAT(2),
        BONES(3),
        IMC(4),
        MUSCLE(5);

        private int value;

        private static Map map = new HashMap<>();

        StatFilter(int value) {
            this.value = value;
        }

        static {
            for (StatFilter pageType : StatFilter.values()) {
                map.put(pageType.value, pageType);
            }
        }

        public static StatFilter valueOf(int statFilter) {
            return (StatFilter) map.get(statFilter);
        }

        public int getValue() {
            return value;
        }
    }
}