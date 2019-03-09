package com.dglozano.escale.ui.main.stats;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.repository.BodyMeasurementRepository;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.ui.Event;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

import static com.dglozano.escale.ui.main.stats.StatsViewModel.StatFilter.WEIGHT;
import static com.dglozano.escale.ui.main.stats.StatsViewModel.StatFilter.valueOf;

public class StatsViewModel extends ViewModel {

    private PatientRepository mPatientRepository;
    private BodyMeasurementRepository mMeasurementsRepository;
    private CompositeDisposable disposables;
    private final MutableLiveData<Event<Integer>> mErrorEvent;
    private final LiveData<List<Entry>> mChartEntriesList;
    private final MutableLiveData<StatFilter> mSelectedStat;
    private final MutableLiveData<Boolean> mFilterExpandedState;
    private List<Entry> weightEntries;
    private List<Entry> waterEntries;
    private List<Entry> fatEntries;
    private List<Entry> bonesEntries;
    private List<Entry> imcEntries;
    private List<Entry> muscleEntries;

    @Inject
    public StatsViewModel(BodyMeasurementRepository bodyMeasurementRepository,
                          PatientRepository patientRepository) {
        mPatientRepository = patientRepository;

        // TODO: BORRAR
        BodyMeasurement[] bodyMeasurements = new BodyMeasurement[]{
                BodyMeasurement.createMockBodyMeasurementForUser(mPatientRepository.getLoggedPatiendId()),
                BodyMeasurement.createMockBodyMeasurementForUser(mPatientRepository.getLoggedPatiendId()),
                BodyMeasurement.createMockBodyMeasurementForUser(mPatientRepository.getLoggedPatiendId()),
                BodyMeasurement.createMockBodyMeasurementForUser(mPatientRepository.getLoggedPatiendId()),
                BodyMeasurement.createMockBodyMeasurementForUser(mPatientRepository.getLoggedPatiendId()),
                BodyMeasurement.createMockBodyMeasurementForUser(mPatientRepository.getLoggedPatiendId()),
                BodyMeasurement.createMockBodyMeasurementForUser(mPatientRepository.getLoggedPatiendId()),
                BodyMeasurement.createMockBodyMeasurementForUser(mPatientRepository.getLoggedPatiendId()),
                BodyMeasurement.createMockBodyMeasurementForUser(mPatientRepository.getLoggedPatiendId()),
                BodyMeasurement.createMockBodyMeasurementForUser(mPatientRepository.getLoggedPatiendId()),
                BodyMeasurement.createMockBodyMeasurementForUser(mPatientRepository.getLoggedPatiendId()),
                BodyMeasurement.createMockBodyMeasurementForUser(mPatientRepository.getLoggedPatiendId()),
                BodyMeasurement.createMockBodyMeasurementForUser(mPatientRepository.getLoggedPatiendId()),
                BodyMeasurement.createMockBodyMeasurementForUser(mPatientRepository.getLoggedPatiendId()),
        };

        Arrays.sort(bodyMeasurements, (o1, o2) ->
                o1.getDate().before(o2.getDate()) ? -1 : o2.getDate().before(o1.getDate()) ? 1 : 0);


        weightEntries = new ArrayList<>();
        waterEntries = new ArrayList<>();
        fatEntries = new ArrayList<>();
        bonesEntries = new ArrayList<>();
        imcEntries = new ArrayList<>();
        muscleEntries = new ArrayList<>();


        for (BodyMeasurement data : bodyMeasurements) {
            weightEntries.add(new Entry(data.getDate().getTime(), data.getWeight()));
            fatEntries.add(new Entry(data.getDate().getTime(), data.getFat()));
            imcEntries.add(new Entry(data.getDate().getTime(), data.getBmi()));
            waterEntries.add(new Entry(data.getDate().getTime(), data.getWater()));
            bonesEntries.add(new Entry(data.getDate().getTime(), data.getBones()));
            muscleEntries.add(new Entry(data.getDate().getTime(), data.getMuscles()));
        }

        mMeasurementsRepository = bodyMeasurementRepository;
        disposables = new CompositeDisposable();
        mErrorEvent = new MutableLiveData<>();

        mFilterExpandedState = new MutableLiveData<>();
        mFilterExpandedState.postValue(true);

        mSelectedStat = new MutableLiveData<>();
        mSelectedStat.postValue(WEIGHT);

        mChartEntriesList = Transformations.map(mSelectedStat, stat -> {
            switch (stat) {
                case WEIGHT:
                    return weightEntries;
                case FAT:
                    return fatEntries;
                case IMC:
                    return imcEntries;
                case BONES:
                    return bonesEntries;
                case WATER:
                    return waterEntries;
                case MUSCLE:
                    return muscleEntries;
                default:
                    return weightEntries;
            }
        });
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }

    public LiveData<Event<Integer>> getErrorEvent() {
        return mErrorEvent;
    }

    public LiveData<List<Entry>> getEntriesForChart() {
        return mChartEntriesList;
    }

    public LiveData<Boolean> getFilterExpansionState() {
        return mFilterExpandedState;
    }

    public void setSelectedStat(int position) {
        mSelectedStat.postValue(valueOf(position));
    }

    public StatFilter getSelectedStat() {
        return mSelectedStat.getValue() == null ? WEIGHT : mSelectedStat.getValue();
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