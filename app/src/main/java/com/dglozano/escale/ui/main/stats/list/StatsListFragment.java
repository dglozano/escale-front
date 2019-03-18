package com.dglozano.escale.ui.main.stats.list;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dglozano.escale.R;
import com.dglozano.escale.ui.main.MainActivityViewModel;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import timber.log.Timber;

public class StatsListFragment extends Fragment {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private Unbinder mViewUnbinder;
    private MainActivityViewModel mMainActivityViewModel;
    private StatsListViewModel mStatsListViewModel;

    public StatsListFragment() {
        // Required empty public constructor
    }

    public static StatsListFragment newInstance() {
        return new StatsListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stats_list, container, false);
        mViewUnbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

//        mStatsListViewModel.getChartEntries().observe(this, entriesList -> {
//            if (entriesList != null && !entriesList.isEmpty()) {
//                refreshChartEntries(entriesList);
//            }
//        });
//
//        mStatsChartViewModel.getFilterExpansionState().observe(this, expanded -> {
//            mFiltersExpandable.setExpanded(expanded == null ? true : expanded);
//        });
//
//        statsFilterRadioGroup.setOnClickedButtonListener((button, position) -> {
//            mStatsChartViewModel.setSelectedStat(position);
//        });
//
//        setupLineChart();
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
        mStatsListViewModel = ViewModelProviders.of(this, mViewModelFactory).get(StatsListViewModel.class);
        mMainActivityViewModel = ViewModelProviders.of(getActivity()).get(MainActivityViewModel.class);
    }
}
