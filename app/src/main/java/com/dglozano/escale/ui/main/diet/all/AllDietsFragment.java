package com.dglozano.escale.ui.main.diet.all;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.dglozano.escale.R;
import com.dglozano.escale.repository.DietRepository;
import com.dglozano.escale.ui.main.MainActivity;
import com.dglozano.escale.ui.main.MainActivityViewModel;
import com.dglozano.escale.ui.main.diet.DietTabAdapter;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import timber.log.Timber;

public class AllDietsFragment extends Fragment {

    @BindView(R.id.recycler_view_diets)
    RecyclerView mRecyclerViewDiets;
    @BindView(R.id.diets_empty_layout)
    RelativeLayout mDietsEmptyLayout;
    @BindView(R.id.diets_swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Inject
    LinearLayoutManager mLayoutManager;
    @Inject
    AllDietsListAdapter mAllDietsListAdapter;
    @Inject
    DefaultItemAnimator mDefaultItemAnimator;
    @Inject
    DividerItemDecoration mDividerItemDecoration;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private Unbinder mViewUnbinder;
    private AllDietsViewModel mAllDietsViewModel;

    public AllDietsFragment() {
        // Required empty public constructor
    }

    public static AllDietsFragment newInstance() {
        return new AllDietsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.sub_fragment_all_diets_list, container, false);
        mViewUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate().");
        mAllDietsViewModel = ViewModelProviders.of(this, mViewModelFactory).get(AllDietsViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupRecyclerList();
        mAllDietsViewModel.getRefreshingListStatus().observe(this, isRefreshing -> {
            mSwipeRefreshLayout.setRefreshing(isRefreshing != null && isRefreshing);
        });
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mAllDietsViewModel.refreshDiets();
        });
    }

    private void setupRecyclerList() {
        mRecyclerViewDiets.setHasFixedSize(true);
        mRecyclerViewDiets.setLayoutManager(mLayoutManager);
        mRecyclerViewDiets.setItemAnimator(mDefaultItemAnimator);
        mRecyclerViewDiets.addItemDecoration(mDividerItemDecoration);
        mRecyclerViewDiets.setAdapter(mAllDietsListAdapter);
        mAllDietsViewModel.getDietsOfLoggedPatient().observe(this, diets -> {
            if(diets != null) {
                mAllDietsListAdapter.setItems(diets);
                if(diets.isEmpty()) {
                    mRecyclerViewDiets.setVisibility(View.GONE);
                    mDietsEmptyLayout.setVisibility(View.VISIBLE);
                } else {
                    mDietsEmptyLayout.setVisibility(View.GONE);
                    mRecyclerViewDiets.setVisibility(View.VISIBLE);
                }
            } else {
                mRecyclerViewDiets.setVisibility(View.GONE);
                mDietsEmptyLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.d("onDestroyView().");
        mViewUnbinder.unbind();
    }
}
