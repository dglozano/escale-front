package com.dglozano.escale.ui.main.stats.chart;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dglozano.escale.R;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.ui.main.MainActivityViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BaseEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;
import dagger.android.support.AndroidSupportInjection;
import timber.log.Timber;

public class StatsChartFragment extends Fragment {

    @BindView(R.id.stats_line_chart)
    LineChart mLineChart;
    @BindView(R.id.expandable_filters_stats)
    ExpandableLayout mFiltersExpandable;
    @BindView(R.id.stats_filter_radio_group)
    RadioRealButtonGroup statsFilterRadioGroup;
    @BindColor(R.color.colorAccent)
    int colorAccent;
    @BindColor(R.color.colorTextDark)
    int textDark;
    @BindColor(R.color.colorTextBlack)
    int textBlack;
    @BindColor(R.color.lightGray)
    int lightGray;
    @BindColor(R.color.lightGrayTransparent)
    int lightGrayTransparent;
    @BindColor(android.R.color.white)
    int white;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    @Inject
    PatientRepository mPatientRepository;

    private Unbinder mViewUnbinder;
    private MainActivityViewModel mMainActivityViewModel;
    private StatsChartViewModel mStatsChartViewModel;

    public StatsChartFragment() {
        // Required empty public constructor
    }

    public static StatsChartFragment newInstance() {
        return new StatsChartFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stats_chart, container, false);
        mViewUnbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mStatsChartViewModel.getChartEntries().observe(this, entriesList -> {
            if (entriesList != null && !entriesList.isEmpty()) {
                refreshChartEntries(entriesList);
            }
        });

        mStatsChartViewModel.getFilterExpansionState().observe(this, expanded -> {
            mFiltersExpandable.setExpanded(expanded == null ? true : expanded);
        });

        statsFilterRadioGroup.setOnClickedButtonListener((button, position) -> {
            mStatsChartViewModel.setSelectedStat(position);
        });

        setupLineChart();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        statsFilterRadioGroup.setPosition(0);
    }

    private void refreshChartEntries(List<Entry> entriesList) {

        entriesList.sort(Comparator.comparingDouble(Entry::getX));

        float dayInMilliseconds = 24 * 3600 * 1000;
        float lastDate = entriesList.get(entriesList.size() - 1).getX();
        float firstDate = lastDate - dayInMilliseconds * 10;

        List<Entry> filteredList = entriesList.stream()
                .filter(entry -> entry.getX() > firstDate)
                .collect(Collectors.toList());

        float xAxisMin = filteredList.get(0).getX() - dayInMilliseconds;
        float xAxisMax = filteredList.get(filteredList.size() - 1).getX() + 2 * dayInMilliseconds;

        filteredList.add(0, new Entry(firstDate - dayInMilliseconds * 2, filteredList.get(0).getY()));

        LineDataSet dataSet = new LineDataSet(filteredList, mStatsChartViewModel.getSelectedStat().toString());
        dataSet.setColor(colorAccent);
        dataSet.setValueTextColor(textDark);
        dataSet.setCircleColor(colorAccent);
        dataSet.setCircleRadius(2.5f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setLineWidth(1.5f);
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSet.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return new DecimalFormat("###,###.#").format(value).replace(",", ".");
            }
        });

        LineData lineData = new LineData(dataSet);

        float maxYValue = entriesList.stream().map(BaseEntry::getY).max(Comparator.comparing(Float::valueOf)).orElse(150f);
        float minYValue = entriesList.stream().map(BaseEntry::getY).min(Comparator.comparing(Float::valueOf)).orElse(0f);
        float axisMax = maxYValue + 2f * (maxYValue - (maxYValue + minYValue) / 2f);
        float axisMin = minYValue - 2f * (((maxYValue + minYValue) / 2f) - minYValue);

        mLineChart.getXAxis().setAxisMinimum(xAxisMin);
        mLineChart.getXAxis().setAxisMaximum(xAxisMax);
        mLineChart.getXAxis().setCenterAxisLabels(true);
        mLineChart.getXAxis().setLabelCount(6, true);

        mLineChart.getAxisLeft().setAxisMaximum(axisMax == axisMin ? axisMax * 2 : axisMax);
        mLineChart.getAxisLeft().setAxisMinimum(axisMin < 0 || axisMin == axisMax ? 0 : axisMin);
        mLineChart.getAxisLeft().setSpaceBottom(0.0f);

        mLineChart.setData(lineData);

        mLineChart.notifyDataSetChanged();

        mLineChart.invalidate();
    }

    private void setupLineChart() {
        mLineChart.setNoDataText("");

        Description description = new Description();
        description.setText("");
        mLineChart.setDescription(description);

        mLineChart.getXAxis().setDrawGridLines(false);
        mLineChart.getXAxis().setLabelRotationAngle(-30f);
        mLineChart.getXAxis().setAxisLineWidth(1.5f);
        mLineChart.getXAxis().setAxisLineColor(lightGray);
        mLineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mLineChart.getXAxis().setGridColor(lightGrayTransparent);
        mLineChart.getXAxis().setDrawGridLines(true);

        mLineChart.getXAxis().setValueFormatter((value, axis) -> {
            Date d = new Date(Float.valueOf(value).longValue());
            return new SimpleDateFormat("dd-MM", Locale.getDefault()).format(d);
        });

        mLineChart.getAxisLeft().setAxisLineWidth(1.5f);
        mLineChart.getAxisLeft().setAxisLineColor(lightGray);
        mLineChart.getAxisLeft().setDrawGridLines(false);
        mLineChart.getAxisLeft().setEnabled(false);

        mLineChart.getAxisRight().setEnabled(false);
        mLineChart.getLegend().setEnabled(false);
        mLineChart.setTouchEnabled(false);

        mLineChart.setGridBackgroundColor(white);

        mLineChart.setViewPortOffsets(0f, 0f, 0f, 80f);
    }

    @OnClick(R.id.toggle_show_filter_button)
    public void onShowFiltersToggleClick(View view) {
        mStatsChartViewModel.toggleFiltersExpansion();
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
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
        mStatsChartViewModel = ViewModelProviders.of(this, mViewModelFactory).get(StatsChartViewModel.class);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMainActivityViewModel = ViewModelProviders.of(getActivity()).get(MainActivityViewModel.class);
    }
}
