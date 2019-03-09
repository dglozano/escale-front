package com.dglozano.escale.ui.main.stats;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.dglozano.escale.R;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.ui.main.MainActivity;
import com.dglozano.escale.ui.main.MainActivityViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;
import dagger.android.support.AndroidSupportInjection;
import timber.log.Timber;

public class StatsFragment extends Fragment {

    @BindView(R.id.stats_line_chart)
    LineChart mLineChart;
    @BindView(R.id.expandable_filters_stats)
    ExpandableLayout mFiltersExpandable;
    @BindView(R.id.toggle_show_filter_button)
    ToggleButton mShowFiltersToggle;
    @BindView(R.id.stats_filter_radio_group)
    RadioRealButtonGroup statsFilterRadioGroup;

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

        mStatsViewModel.getEntriesForChart().observe(this, entriesList -> {
            if(entriesList != null) {
                float firstDate = !entriesList.isEmpty() ? entriesList.get(0).getX() : 0f;
                LineDataSet dataSet = new LineDataSet(entriesList, mStatsViewModel.getSelectedStat().toString());
                dataSet.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.colorTextDark));
                dataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                dataSet.setCircleHoleRadius(1.5f);
                dataSet.setValueTextSize(8f);
                dataSet.setLineWidth(1.5f);
                dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
                dataSet.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) ->
                        entry.getX() == firstDate ? "" : value + "");

                LineData lineData = new LineData(dataSet);

                mLineChart.setData(lineData);
                mLineChart.notifyDataSetChanged();
                mLineChart.invalidate();
            }
        });

        mStatsViewModel.getFilterExpansionState().observe(this, expanded -> {
            mFiltersExpandable.setExpanded(expanded == null ? true : expanded);
        });

        statsFilterRadioGroup.setOnClickedButtonListener((button, position) -> {
            mStatsViewModel.setSelectedStat(position);
        });

        setupLineChart();
    }

    private void setupLineChart() {
        mLineChart.setNoDataText(getString(R.string.no_stats_yet_text_in_chart));
        mLineChart.setNoDataTextColor(ContextCompat.getColor(getContext(), R.color.lightGray));

        Description description = new Description();
        description.setText("");
        mLineChart.setDescription(description);

        mLineChart.getXAxis().setDrawGridLines(false);
        mLineChart.getXAxis().setLabelRotationAngle(-30f);
        mLineChart.getXAxis().setAxisLineWidth(1.5f);
        mLineChart.getXAxis().setAxisLineColor(ContextCompat.getColor(getContext(), R.color.lightGray));
        mLineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mLineChart.getXAxis().setSpaceMin(5f);
        mLineChart.getXAxis().setSpaceMax(5f);
        mLineChart.getXAxis().setValueFormatter((value, axis) -> {
            Date d = new Date(Float.valueOf(value).longValue());
            return new SimpleDateFormat("dd-MM", Locale.getDefault()).format(d);
        });

        mLineChart.getAxisLeft().setAxisLineWidth(1.5f);
        mLineChart.getAxisLeft().setAxisLineColor(ContextCompat.getColor(getContext(), R.color.lightGray));
        mLineChart.getAxisLeft().setDrawGridLines(false);

        mLineChart.getAxisRight().setEnabled(false);
        mLineChart.getLegend().setEnabled(false);
        mLineChart.setTouchEnabled(false);

        mLineChart.invalidate();
    }

    @OnClick(R.id.toggle_show_filter_button)
    public void onShowFiltersToggleClick(View view) {
        mStatsViewModel.toggleFiltersExpansion();
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
