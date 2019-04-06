package com.dglozano.escale.ui.main.diet.all;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dglozano.escale.R;
import com.dglozano.escale.ui.main.diet.show.ShowDietPdfActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import timber.log.Timber;

public class AllDietsFragment extends Fragment {

    public static final int SHOW_PDF_CODE = 999;

    @BindView(R.id.recycler_view_diets)
    RecyclerView mRecyclerViewDiets;
    @BindView(R.id.diets_swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

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
        View view = inflater.inflate(R.layout.fragment_diet_all, container, false);
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
        mAllDietsViewModel = ViewModelProviders.of(getActivity(), mViewModelFactory).get(AllDietsViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupRecyclerList();
        mAllDietsViewModel.getRefreshingListStatus().observe(this, isRefreshing -> {
            mSwipeRefreshLayout.setRefreshing(isRefreshing != null && isRefreshing);
        });
        mAllDietsViewModel.getShowPdfEvent().observe(this, pdfEvent -> {
            Timber.d("Pdf event fired");
            if (pdfEvent != null && !pdfEvent.hasBeenHandled()) {
                Intent intent = new Intent(getActivity(), ShowDietPdfActivity.class);
                intent.putExtra("diet_file_path", pdfEvent.handleContent().getAbsolutePath());
                startActivityForResult(intent, SHOW_PDF_CODE);
            }
        });
        mAllDietsViewModel.getErrorEvent().observe(this, errorEvent -> {
            if (errorEvent != null && errorEvent.peekContent() != null && !errorEvent.hasBeenHandled())
                Snackbar.make(getActivity().findViewById(android.R.id.content),
                        errorEvent.handleContent(), Snackbar.LENGTH_SHORT).show();
        });
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mAllDietsViewModel.refreshDiets();
        });
    }

    private void setupRecyclerList() {
        mRecyclerViewDiets.setHasFixedSize(true);
        mRecyclerViewDiets.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerViewDiets.setItemAnimator(mDefaultItemAnimator);
        mRecyclerViewDiets.addItemDecoration(mDividerItemDecoration);
        mAllDietsListAdapter.setDietClickListener(diet -> mAllDietsViewModel.openOldDietFile(diet));
        mRecyclerViewDiets.setAdapter(mAllDietsListAdapter);
        mAllDietsViewModel.getDietsOfLoggedPatient().observe(this, diets -> {
            if (diets != null) {
                diets.forEach(diet -> Timber.d("Diet %s - Status %s", diet.getFileName(), diet.getFileStatus()));
                mAllDietsListAdapter.setItems(diets);
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
