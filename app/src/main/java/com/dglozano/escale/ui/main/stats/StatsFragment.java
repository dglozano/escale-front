package com.dglozano.escale.ui.main.stats;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.dglozano.escale.R;
import com.dglozano.escale.ui.main.stats.chart.StatsChartFragment;
import com.dglozano.escale.ui.main.stats.list.StatsListFragment;
import com.dglozano.escale.util.ui.FragmentWithViewPager;
import com.dglozano.escale.util.ui.MyTabAdapter;
import com.google.android.material.tabs.TabLayout;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import timber.log.Timber;

public class StatsFragment extends Fragment implements FragmentWithViewPager {

    @BindView(R.id.stats_view_pager_tabs)
    ViewPager mTabsViewPager;
    @BindView(R.id.stats_chart_or_list_tablayout)
    TabLayout mTabLayout;
    @BindView(R.id.no_stats_layout)
    RelativeLayout mNoStatsLayout;
    @BindView(R.id.stats_main_container)
    RelativeLayout mStatsMainContainer;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    @Inject
    MyTabAdapter mTabsAdapter;

    private Unbinder mViewUnbinder;
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

        setupViewPager(mTabsViewPager);

        mStatsViewModel.areMeasurementsEmpty().observe(this, measurementsEmpty -> {
            if (measurementsEmpty != null && !measurementsEmpty) {
                mStatsMainContainer.setVisibility(View.VISIBLE);
                mTabLayout.setVisibility(View.VISIBLE);
                mNoStatsLayout.setVisibility(View.GONE);
                mTabLayout.setupWithViewPager(mTabsViewPager);
            } else {
                mNoStatsLayout.setVisibility(View.VISIBLE);
                mStatsMainContainer.setVisibility(View.GONE);
                mTabLayout.setVisibility(View.GONE);
            }
        });

        return view;
    }

    private void setupViewPager(ViewPager viewPager) {
        mTabsAdapter.addFragment(new StatsChartFragment(), getString(R.string.chart_stats_title));
        mTabsAdapter.addFragment(new StatsListFragment(), getString(R.string.list_stats_title));
        viewPager.setAdapter(mTabsAdapter);
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
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.clear();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStatsViewModel = ViewModelProviders.of(this, mViewModelFactory).get(StatsViewModel.class);
    }

    @Override
    public MyTabAdapter getPagerAdapter() {
        return mTabsAdapter;
    }
}
