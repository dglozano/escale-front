package com.dglozano.escale.ui.main.stats.list;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dglozano.escale.R;
import com.dglozano.escale.ui.main.MainActivityViewModel;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import timber.log.Timber;

public class StatsListFragment extends Fragment {

    @BindView(R.id.stats_list_recycler_view)
    RecyclerView mRecyclerView;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    @Inject
    StatsListAdapter mStatsAdapter;
    @Inject
    DefaultItemAnimator mDefaultItemAnimator;
    @Inject
    DividerItemDecoration mDividerItemDecoration;

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
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(mDefaultItemAnimator);
        mRecyclerView.addItemDecoration(mDividerItemDecoration);
        mRecyclerView.setAdapter(mStatsAdapter);
        mStatsListViewModel.getStatsOfLoggedUser().observe(this, stats -> {
            if (stats != null) {
                mStatsAdapter.setItems(stats);
            }
        });
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
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMainActivityViewModel = ViewModelProviders.of(getActivity()).get(MainActivityViewModel.class);
    }
}
