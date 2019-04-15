package com.dglozano.escale.ui.main.stats.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dglozano.escale.R;
import com.dglozano.escale.db.entity.MeasurementForecast;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.ui.main.MainActivity;
import com.dglozano.escale.ui.main.MainActivityViewModel;
import com.dglozano.escale.util.Constants;
import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BaseEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.ViewPortHandler;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
    @BindColor(R.color.colorPrimaryLight)
    int colorPrimaryLight;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    @Inject
    PatientRepository mPatientRepository;
    @Inject
    MainActivity mMainActivity;
    @Inject
    IValueFormatter valueFormatter;

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

    private void refreshChartEntries(List<Entry> entriesList) {

        if (mLineChart.getLineData() != null) mLineChart.clear();

        entriesList.sort(Comparator.comparingDouble(Entry::getX));
        Entry lastEntry = entriesList.get(entriesList.size() - 1);

        setChartDescription();

        float dayInMilliseconds = 24 * 3600 * 1000;
        float lastDate = entriesList.get(entriesList.size() - 1).getX();
        float firstDate = lastDate - dayInMilliseconds * 10;

        List<Entry> filteredList = entriesList.stream()
                .filter(entry -> entry.getX() > firstDate)
                .collect(Collectors.toList());

        float xAxisMin = filteredList.get(0).getX() - dayInMilliseconds;
        float xAxisMaxVisible = filteredList.get(filteredList.size() - 1).getX() + 2 * dayInMilliseconds;
        float xAxisMax = filteredList.get(filteredList.size() - 1).getX() + (Constants.FORECAST_AMOUNT / 2f) * dayInMilliseconds;

        filteredList.add(0, new Entry(firstDate - dayInMilliseconds * 2, filteredList.get(0).getY()));

        float maxYValue = entriesList.stream().map(BaseEntry::getY).max(Comparator.comparing(Float::valueOf)).orElse(150f);
        float minYValue = entriesList.stream().map(BaseEntry::getY).min(Comparator.comparing(Float::valueOf)).orElse(0f);
        calculateLeftAxisMaxAndMin(minYValue, maxYValue);

        LineDataSet dataSet = new LineDataSet(filteredList, mStatsChartViewModel.getSelectedStat().toString());
        setDefaultDataSetConfig(dataSet);

        mLineChart.getXAxis().setAxisMinimum(xAxisMin);
        mLineChart.getXAxis().setAxisMaximum(xAxisMax);
        mLineChart.getXAxis().setCenterAxisLabels(true);
        mLineChart.getXAxis().setLabelCount(6, true);
        mLineChart.getAxisLeft().setSpaceBottom(0.0f);

        LineData lineData = mLineChart.getLineData();
        if (lineData == null) {
            lineData = new LineData(dataSet);
        } else {
            ILineDataSet previousDataSet = lineData.getDataSetByLabel(dataSet.getLabel(), true);
            if (previousDataSet != null) lineData.removeDataSet(previousDataSet);
            lineData.addDataSet(dataSet);
        }

        mLineChart.setData(lineData);

        if (mStatsChartViewModel.getSelectedStat().equals(StatsChartViewModel.StatFilter.WEIGHT) &&
                mStatsChartViewModel.getGoalEntry().getValue() != null) {
            drawGoalLine(mStatsChartViewModel.getGoalEntry().getValue(), lineData, xAxisMin, xAxisMax, xAxisMaxVisible);
        }

        if (mStatsChartViewModel.getMeasurementForecast().getValue() != null
                && mStatsChartViewModel.getMeasurementForecast().getValue().isPresent()) {
            drawForecastLine(lastEntry,
                    mStatsChartViewModel.getMeasurementForecast().getValue().get(),
                    mStatsChartViewModel.getSelectedStat(), lineData);
        }

        mLineChart.moveViewToX(xAxisMin);
        mLineChart.setVisibleXRangeMaximum(xAxisMaxVisible - xAxisMin);

        mLineChart.notifyDataSetChanged();
        mLineChart.invalidate();
    }

    private void drawForecastLine(Entry lastEntry, MeasurementForecast mf,
                                  StatsChartViewModel.StatFilter selectedStat,
                                  LineData lineData) {
        if (lineData == null || lineData.getDataSetByLabel(Constants.FORECAST_DATASET, true) == null) {
            List<Entry> forecastEntryList = new ArrayList<>();
            forecastEntryList.add(lastEntry);
            forecastEntryList.addAll(getExpSmoothForecastEntriesFor(lastEntry, mf, selectedStat));
            LineDataSet forecastDataSet = new LineDataSet(forecastEntryList, Constants.FORECAST_DATASET);

            setDefaultDataSetConfig(forecastDataSet);
            forecastDataSet.setDrawCircleHole(false);
            forecastDataSet.setDrawCircles(false);
            forecastDataSet.enableDashedLine(10f, 10f, 0);
            forecastDataSet.setDrawValues(false);
            forecastDataSet.setColor(lightGray);

            if (lineData == null) {
                lineData = new LineData(forecastDataSet);
            } else {
                ILineDataSet previousForecastDataSet = lineData.getDataSetByLabel(forecastDataSet.getLabel(), true);
                if (previousForecastDataSet != null)
                    lineData.removeDataSet(previousForecastDataSet);
                lineData.addDataSet(forecastDataSet);
            }

            mLineChart.setData(lineData);
            mLineChart.notifyDataSetChanged();
            mLineChart.invalidate();
        }

    }

    private List<Entry> getExpSmoothForecastEntriesFor(Entry lastEntry, MeasurementForecast measurementForecast, StatsChartViewModel.StatFilter selectedStat) {
        long dayInMillis = 24 * 3600 * 1000;
        switch (selectedStat) {
            case FAT:
                return measurementForecast.getNextDaysPredictions().stream()
                        .map(prediction ->
                                new Entry(lastEntry.getX() + prediction.getDaysOffset() * dayInMillis, (float) prediction.getFat()))
                        .collect(Collectors.toList());
            case WATER:
                return measurementForecast.getNextDaysPredictions().stream()
                        .map(prediction ->
                                new Entry(lastEntry.getX() + prediction.getDaysOffset() * dayInMillis, (float) prediction.getWater()))
                        .collect(Collectors.toList());
            case MUSCLE:
                return measurementForecast.getNextDaysPredictions().stream()
                        .map(prediction ->
                                new Entry(lastEntry.getX() + prediction.getDaysOffset() * dayInMillis, (float) prediction.getMuscles()))
                        .collect(Collectors.toList());
            case IMC:
                return measurementForecast.getNextDaysPredictions().stream()
                        .map(prediction ->
                                new Entry(lastEntry.getX() + prediction.getDaysOffset() * dayInMillis, (float) prediction.getBmi()))
                        .collect(Collectors.toList());
            case WEIGHT:
            default:
                return measurementForecast.getNextDaysPredictions().stream()
                        .map(prediction ->
                                new Entry(lastEntry.getX() + prediction.getDaysOffset() * dayInMillis, (float) prediction.getWeight()))
                        .collect(Collectors.toList());
        }
    }

    private void drawGoalLine(Entry goalEntry, LineData lineData, float xAxisMin, float xAxisMax, float xAxisMaxVisible) {
        if (lineData != null && lineData.getDataSetByLabel(Constants.GOAL_DATASET, true) == null) {
            float minY = mLineChart.getAxisLeft().getAxisMinimum();
            float maxY = mLineChart.getAxisRight().getAxisMaximum();

            float dayInMilliseconds = 24 * 3600 * 1000;

            List<Entry> goalEntryList = new ArrayList<>();
            LineDataSet goalDataSet;

            float goalLabelPosition = (xAxisMaxVisible - xAxisMin) * 0.15f + xAxisMin;

            LineDataSet goalLabelDataSet = new LineDataSet(
                    Collections.singletonList(new Entry(goalLabelPosition, goalEntry.getY())), Constants.GOAL_LABEL_DATASET);
            setDefaultDataSetConfig(goalLabelDataSet);
            goalLabelDataSet.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> String.format("META %s kg.", value));
            goalLabelDataSet.setDrawCircles(false);

            goalEntryList.add(new Entry(xAxisMin - dayInMilliseconds * 2, goalEntry.getY()));
            goalEntryList.add(goalEntry);
            goalEntryList.add(new Entry(xAxisMax + dayInMilliseconds * 2, goalEntry.getY()));
            goalDataSet = new LineDataSet(goalEntryList, Constants.GOAL_DATASET);
            setDefaultDataSetConfig(goalDataSet);
            goalDataSet.setMode(LineDataSet.Mode.LINEAR);
            goalDataSet.setCircleRadius(4f);
            goalDataSet.setColor(colorPrimaryLight);
            goalDataSet.setCircleColor(colorPrimaryLight);
            goalDataSet.enableDashedLine(10f, 10f, 0);

            boolean goalLineBelowLimits = goalEntry.getY() < minY;
            boolean goalLineAboveLimits = goalEntry.getY() > maxY;

            if (goalLineBelowLimits) {
                calculateLeftAxisMaxAndMin(goalEntry.getY(), maxY);
            } else if (goalLineAboveLimits) {
                calculateLeftAxisMaxAndMin(minY, goalEntry.getY());
            }

            ILineDataSet previousGoalDataSet = lineData.getDataSetByLabel(goalDataSet.getLabel(), true);
            ILineDataSet previousGoalLabelDataSet = lineData.getDataSetByLabel(goalLabelDataSet.getLabel(), true);
            if (previousGoalDataSet != null)
                lineData.removeDataSet(previousGoalDataSet);
            if (previousGoalLabelDataSet != null)
                lineData.removeDataSet(previousGoalLabelDataSet);
            lineData.addDataSet(goalLabelDataSet);
            lineData.addDataSet(goalDataSet);

            mLineChart.setData(lineData);
            mLineChart.notifyDataSetChanged();
            mLineChart.invalidate();
        }
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
        mLineChart.setTouchEnabled(true);
        mLineChart.setHorizontalScrollBarEnabled(true);

        mLineChart.setGridBackgroundColor(white);

        float yValueOffset = getResources().getDimension(R.dimen.activity_vertical_margin_almost_nothing);
        mLineChart.setRenderer(new MyLineChartRenderer(yValueOffset, mLineChart, mLineChart.getAnimator(), mLineChart.getViewPortHandler()));

        mLineChart.setViewPortOffsets(0f, 0f, 0f, 80f);
    }

    private void setChartDescription() {
        Description description = new Description();

        switch (mStatsChartViewModel.getSelectedStat()) {
            case WEIGHT:
                description.setText("Peso (kg.)");
                break;
            case WATER:
                description.setText("Agua corporal (%)");
                break;
            case FAT:
                description.setText("Grasa corporal (%)");
                break;
            case IMC:
                description.setText("IMC");
                break;
            case MUSCLE:
                description.setText("Masa muscular (%)");
                break;
            default:
                description.setText("");
        }
        description.setEnabled(true);
        description.setTextAlign(Paint.Align.RIGHT);
        description.setTextColor(textDark);
        description.setTextSize(12f);
        description.setXOffset(10f);
        description.setYOffset(10f);

        mLineChart.setDescription(description);
    }

    private void setDefaultDataSetConfig(LineDataSet dataSet) {
        dataSet.setColor(colorAccent);
        dataSet.setValueTextColor(textDark);
        dataSet.setCircleColor(colorAccent);
        dataSet.setCircleRadius(2.5f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setLineWidth(1.5f);
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSet.setValueFormatter(valueFormatter);
    }

    private void calculateLeftAxisMaxAndMin(float newMinY, float newMaxY) {
        float range = newMaxY - newMinY;
        float axisMin = newMinY - range * 1.10f;
        float axisMax = newMaxY + range * 1.10f;
        switch (mStatsChartViewModel.getSelectedStat()) {
            case IMC:
            case FAT:
            case WATER:
            case MUSCLE:
                mLineChart.getAxisLeft().setAxisMinimum(axisMin < 0 ? 0 : range == 0 ? axisMin * 0.95f : axisMin);
                mLineChart.getAxisLeft().setAxisMaximum(axisMax > 100 ? 100 : range == 0 ? axisMax * 1.05f : axisMax);
                break;
            case WEIGHT:
            default:
                mLineChart.getAxisLeft().setAxisMinimum(axisMin < 0 ? 0 : range == 0 ? axisMin * 0.95f : axisMin);
                mLineChart.getAxisLeft().setAxisMaximum(range == 0 ? axisMax * 1.05f : axisMax);
                break;
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        statsFilterRadioGroup.setPosition(0);
    }

    @OnClick(R.id.toggle_show_filter_button)
    public void onShowFiltersToggleClick(View view) {
        mStatsChartViewModel.toggleFiltersExpansion();
    }

    @Override
    public void onAttach(@NonNull Context context) {
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
        mMainActivityViewModel = ViewModelProviders.of(mMainActivity).get(MainActivityViewModel.class);
    }

    private class MyLineChartRenderer extends LineChartRenderer {

        private float yOffset;

        private MyLineChartRenderer(float yOffset, LineDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
            super(chart, animator, viewPortHandler);
            this.yOffset = yOffset;
        }

        @Override
        public void drawValue(Canvas c, IValueFormatter formatter, float value, Entry entry, int dataSetIndex, float x, float y, int color) {
            super.drawValue(c, formatter, value, entry, dataSetIndex, x, y - this.yOffset, color);
        }

    }
}
