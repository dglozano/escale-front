package com.dglozano.escale.ui.doctor.main.home.alerts;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.dglozano.escale.R;
import com.dglozano.escale.ui.main.MainActivityViewModel;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import timber.log.Timber;

public class DoctorHomeAlertListFragment extends Fragment {

    @BindView(R.id.alerts_list_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.no_alerts_layout)
    RelativeLayout mNoAlertsLayout;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    @Inject
    DoctorHomeAlertListAdapter mAlertsAdapter;
    @Inject
    DefaultItemAnimator mDefaultItemAnimator;
    @Inject
    DividerItemDecoration mDividerItemDecoration;

    private Unbinder mViewUnbinder;
    private MainActivityViewModel mMainActivityViewModel;
    private DoctorHomeAlertListViewModel mDoctorHomeAlertListViewModel;

    public DoctorHomeAlertListFragment() {
        // Required empty public constructor
    }

    public static DoctorHomeAlertListFragment newInstance() {
        return new DoctorHomeAlertListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_doctor_alerts, container, false);
        mViewUnbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(mDefaultItemAnimator);
        mRecyclerView.addItemDecoration(mDividerItemDecoration);
        mAlertsAdapter.setAlertClickListener(alert ->
                mDoctorHomeAlertListViewModel.toggleSeenByDoctor(alert));
        mRecyclerView.setAdapter(mAlertsAdapter);

        mDoctorHomeAlertListViewModel.getAlertsOfPatient().observe(this, alerts -> {
            if (alerts == null || alerts.isEmpty()) {
                mNoAlertsLayout.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            } else {
                mNoAlertsLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mAlertsAdapter.setItems(alerts);
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
        mDoctorHomeAlertListViewModel = ViewModelProviders.of(this, mViewModelFactory).get(DoctorHomeAlertListViewModel.class);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMainActivityViewModel = ViewModelProviders.of(getActivity(), mViewModelFactory).get(MainActivityViewModel.class);
    }
}
