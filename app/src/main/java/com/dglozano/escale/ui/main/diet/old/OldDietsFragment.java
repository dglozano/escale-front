package com.dglozano.escale.ui.main.diet.old;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import com.dglozano.escale.db.entity.Diet;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import timber.log.Timber;

public class OldDietsFragment extends Fragment {

    @BindView(R.id.recycler_view_diets)
    RecyclerView mRecyclerViewDiets;
    @BindView(R.id.diets_empty_layout)
    RelativeLayout mDietsEmptyLayout;
    @BindView(R.id.diets_swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Inject
    LinearLayoutManager mLayoutManager;
    @Inject
    OldDietsListAdapter mOldDietsListAdapter;
    @Inject
    DefaultItemAnimator mDefaultItemAnimator;
    @Inject
    DividerItemDecoration mDividerItemDecoration;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private Unbinder mViewUnbinder;
    private OldDietsViewModel mOldDietsViewModel;

    public OldDietsFragment() {
        // Required empty public constructor
    }

    public static OldDietsFragment newInstance() {
        return new OldDietsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.sub_fragment_old_diets_list, container, false);
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
        mOldDietsViewModel = ViewModelProviders.of(getActivity(), mViewModelFactory).get(OldDietsViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupRecyclerList();
        mOldDietsViewModel.getRefreshingListStatus().observe(this, isRefreshing -> {
            mSwipeRefreshLayout.setRefreshing(isRefreshing != null && isRefreshing);
        });
        mOldDietsViewModel.getShowPdfEvent().observe(this, pdfEvent -> {
            Timber.d("Pdf event fired");
            if (pdfEvent != null && !pdfEvent.hasBeenHandled()) {
                Intent intent = new Intent(getActivity(), OldDietPdfActivity.class);
                intent.putExtra("diet_file_path", pdfEvent.handleContent().getAbsolutePath());
                startActivity(intent);
            }
        });
        mOldDietsViewModel.getErrorEvent().observe(this, errorEvent -> {
            if (errorEvent != null && errorEvent.peekContent() != null && !errorEvent.hasBeenHandled())
                Snackbar.make(getActivity().findViewById(android.R.id.content),
                        errorEvent.handleContent(), Snackbar.LENGTH_SHORT).show();
        });
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mOldDietsViewModel.refreshDiets();
        });
    }

    private void setupRecyclerList() {
        mRecyclerViewDiets.setHasFixedSize(true);
        mRecyclerViewDiets.setLayoutManager(mLayoutManager);
        mRecyclerViewDiets.setItemAnimator(mDefaultItemAnimator);
        mRecyclerViewDiets.addItemDecoration(mDividerItemDecoration);
        mOldDietsListAdapter.setDietClickListener(new OldDietsListAdapter.DietClickListener() {
            @Override
            public void onClick(Diet diet) {
                mOldDietsViewModel.openOldDietFile(diet);
            }
        });
        mRecyclerViewDiets.setAdapter(mOldDietsListAdapter);
        mOldDietsViewModel.getDietsOfLoggedPatient().observe(this, diets -> {
            if (diets != null) {
                diets.forEach(diet -> Timber.d("Diet %s - Status %s", diet.getFileName(), diet.getFileStatus()));
                mOldDietsListAdapter.setItems(diets);
                if (diets.isEmpty()) {
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
