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
import com.dglozano.escale.util.Constants;
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

        mStatsViewModel.getChartEntries().observe(this, entriesList -> {
            if(entriesList != null && !entriesList.isEmpty()) {
                refreshChartEntries(entriesList);
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

    private void refreshChartEntries(List<Entry> entriesList) {
        LineDataSet dataSet = new LineDataSet(entriesList, mStatsViewModel.getSelectedStat().toString());
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.colorTextDark));
        dataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        dataSet.setCircleHoleRadius(1.5f);
        dataSet.setValueTextSize(8f);
        dataSet.setLineWidth(1.5f);
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSet.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return new DecimalFormat("###,###.##").format(value).replace(",", ".");
            }
        });

        LineData lineData = new LineData(dataSet);

        float dayInMilliseconds = 24 * 3600 * 1000;
        float firstDate = entriesList.get(0).getX();
        float lastDate = entriesList.get(entriesList.size() - 1).getX();
        float daysAfterLast = Constants.BODY_MEASUREMENTS_DEFAULT_LIMIT + 1 - entriesList.size();

        float maxYValue = entriesList.stream().map(BaseEntry::getY).max(Comparator.comparing(Float::valueOf)).orElse(150f);
        float minYValue = entriesList.stream().map(BaseEntry::getY).min(Comparator.comparing(Float::valueOf)).orElse(0f);
        float axisMax = maxYValue + 1.5f * (maxYValue - (maxYValue + minYValue) / 2f);
        float axisMin = minYValue - 1.5f * (((maxYValue + minYValue) / 2f) - minYValue);

        mLineChart.setData(lineData);

        mLineChart.getXAxis().setAxisMinimum(firstDate - dayInMilliseconds);
        mLineChart.getXAxis().setAxisMaximum(lastDate + dayInMilliseconds * daysAfterLast);
        mLineChart.getAxisLeft().setAxisMaximum(axisMax == axisMin ? axisMax * 2 : axisMax );
        mLineChart.getAxisLeft().setAxisMinimum(axisMin < 0 || axisMin == axisMax ? 0 : axisMin);

        mLineChart.notifyDataSetChanged();
        mLineChart.invalidate();
    }

    private void setupLineChart() {
        mLineChart.setNoDataText("");
        mLineChart.setNoDataTextColor(ContextCompat.getColor(getContext(), R.color.lightGray));

        Description description = new Description();
        description.setText("");
        mLineChart.setDescription(description);

        mLineChart.getXAxis().setDrawGridLines(false);
        mLineChart.getXAxis().setLabelRotationAngle(-30f);
        mLineChart.getXAxis().setAxisLineWidth(1.5f);
        mLineChart.getXAxis().setAxisLineColor(ContextCompat.getColor(getContext(), R.color.lightGray));
        mLineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        mLineChart.getXAxis().setValueFormatter((value, axis) -> {
            Date d = new Date(Float.valueOf(value).longValue());
            return new SimpleDateFormat("dd-MM", Locale.getDefault()).format(d);
        });

        mLineChart.getAxisLeft().setAxisLineWidth(1.5f);
        mLineChart.getAxisLeft().setAxisLineColor(ContextCompat.getColor(getContext(), R.color.lightGray));
        mLineChart.getAxisLeft().setDrawGridLines(false);

        mLineChart.getAxisRight().setEnabled(false);
        mLineChart.getLegend().setEnabled(false);
        mLineChart.setHighlightPerTapEnabled(false);
        mLineChart.setHighlightPerDragEnabled(false);
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
