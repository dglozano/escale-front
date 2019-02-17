package com.dglozano.escale.ui.main.diet;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dglozano.escale.R;
import com.dglozano.escale.repository.DietRepository;
import com.dglozano.escale.ui.main.MainActivity;
import com.dglozano.escale.ui.main.MainActivityViewModel;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import timber.log.Timber;


public class DietFragment extends Fragment {

    @BindView(R.id.recycler_view_diets)
    RecyclerView mRecyclerViewDiets;

    @Inject
    LinearLayoutManager mLayoutManager;
    @Inject
    DietListAdapter mDietListAdapter;
    @Inject
    DefaultItemAnimator mDefaultItemAnimator;
    @Inject
    DividerItemDecoration mDividerItemDecoration;
    @Inject
    DietRepository dietRepository;
    @Inject
    MainActivity mMainActivity;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private Unbinder mViewUnbinder;
    private DietViewModel mDietViewModel;
    private MainActivityViewModel mMainActivityViewModel;

    public DietFragment() {
        // Required empty public constructor
    }

    public static DietFragment newInstance() {
        return new DietFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_diet, container, false);
        mViewUnbinder = ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
        mMainActivityViewModel = ViewModelProviders.of(getActivity()).get(MainActivityViewModel.class);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate().");
        mDietViewModel = ViewModelProviders.of(this, mViewModelFactory).get(DietViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRecyclerViewDiets.setHasFixedSize(true);
        mRecyclerViewDiets.setLayoutManager(mLayoutManager);
        mRecyclerViewDiets.setItemAnimator(mDefaultItemAnimator);
        mRecyclerViewDiets.addItemDecoration(mDividerItemDecoration);
        mRecyclerViewDiets.setAdapter(mDietListAdapter);
        mDietViewModel.getDietsOfLoggedPatient().observe(this, diets -> {
            if(diets != null && !diets.isEmpty()) {
                mDietListAdapter.setItems(diets);
            } else {
                Timber.d("No diets yet");
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
