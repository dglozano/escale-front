package com.dglozano.escale.ui.main.stats;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dglozano.escale.R;
import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.ui.main.MainActivity;
import com.dglozano.escale.ui.main.MainActivityViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import timber.log.Timber;

public class StatsFragment extends Fragment {

    @BindView(R.id.stats_line_chart)
    LineChart mLineChart;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    @Inject
    PatientRepository mPatientRepository;

    private Unbinder mViewUnbinder;
    private MainActivityViewModel mMainActivityViewModel;
    private StatsViewModel mStatsViewModel;

    public StatsFragment() {
        // Required empty public constructor
    }

    public static StatsFragment newInstance() {
        return new StatsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        mViewUnbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        BodyMeasurement[] bodyMeasurements = new BodyMeasurement[] {
                BodyMeasurement.createMockBodyMeasurementForUser(mPatientRepository.getLoggedPatiendId()),
                BodyMeasurement.createMockBodyMeasurementForUser(mPatientRepository.getLoggedPatiendId()),
                BodyMeasurement.createMockBodyMeasurementForUser(mPatientRepository.getLoggedPatiendId()),
                BodyMeasurement.createMockBodyMeasurementForUser(mPatientRepository.getLoggedPatiendId()),
                BodyMeasurement.createMockBodyMeasurementForUser(mPatientRepository.getLoggedPatiendId()),
                BodyMeasurement.createMockBodyMeasurementForUser(mPatientRepository.getLoggedPatiendId()),
        };


        List<Entry> entries = new ArrayList<Entry>();

        int i =0;
        for (BodyMeasurement data : bodyMeasurements) {

            // turn your data into Entry objects
            entries.add(new Entry(i++, data.getWeight()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Weight"); // add entries to dataset
        dataSet.setColor(R.color.blue);
        dataSet.setValueTextColor(R.color.black); // styling, ...

        LineData lineData = new LineData(dataSet);
        mLineChart.setData(lineData);
        mLineChart.invalidate(); // refresh
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
        mMainActivityViewModel = ViewModelProviders.of((MainActivity) context).get(MainActivityViewModel.class);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.d("onDestroyView().");
        mViewUnbinder.unbind();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate().");
        mStatsViewModel = ViewModelProviders.of(getActivity(), mViewModelFactory).get(StatsViewModel.class);
    }
}
