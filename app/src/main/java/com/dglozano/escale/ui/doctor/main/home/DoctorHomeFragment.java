package com.dglozano.escale.ui.doctor.main.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dglozano.escale.R;
import com.dglozano.escale.ui.BaseActivity;
import com.dglozano.escale.ui.doctor.main.add_goal.AddGoalActivity;
import com.dglozano.escale.ui.doctor.main.home.alerts.DoctorHomeAlertListFragment;
import com.dglozano.escale.ui.doctor.main.home.profile.DoctorHomeProfileFragment;
import com.dglozano.escale.ui.main.MainActivityViewModel;
import com.dglozano.escale.util.Constants;
import com.dglozano.escale.util.ui.FragmentWithViewPager;
import com.dglozano.escale.util.ui.MyTabAdapter;
import com.google.android.material.snackbar.Snackbar;
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

public class DoctorHomeFragment extends Fragment implements FragmentWithViewPager {

    @BindView(R.id.profile_or_alerts_tablayout_viewpager)
    ViewPager mTabsViewPager;
    @BindView(R.id.profile_or_alerts_tablayout)
    TabLayout mTabLayout;
    @BindView(R.id.doctor_home_progressbar)
    ProgressBar progressBar;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    @Inject
    MyTabAdapter mTabsAdapter;

    private Unbinder mViewUnbinder;
    private MainActivityViewModel mMainActivityViewModel;
    private DoctorHomeViewModel mDoctorHomeViewModel;

    public DoctorHomeFragment() {
        // Required empty public constructor
    }

    public static DoctorHomeFragment newInstance() {
        return new DoctorHomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_doctor, container, false);
        mViewUnbinder = ButterKnife.bind(this, view);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViewPager(mTabsViewPager);

        mMainActivityViewModel.isRefreshing().observe(this, isRefreshing -> {
            if (isRefreshing == null || isRefreshing) {
                mTabsViewPager.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            } else {
                mTabsViewPager.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });

        mDoctorHomeViewModel.getUnseenAlertsForDoctor().observe(this, unseenAlerts -> {
            if (unseenAlerts != null) {
                TabLayout.Tab tab = mTabLayout.getTabAt(1);
                if (tab != null && tab.getCustomView() != null) {
                    TextView badge = tab.getCustomView().findViewById(R.id.my_tab_badge);
                    View v = tab.getCustomView().findViewById(R.id.my_tab_layout_badge_container);
                    if (badge != null && v != null) {
                        if (unseenAlerts > 0) {
                            badge.setText(unseenAlerts + "");
                            v.setVisibility(View.VISIBLE);
                        } else {
                            v.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_doctor_profile_mark_as_seen) {
            mDoctorHomeViewModel.markAllAsSeen();
        } else if (id == R.id.menu_doctor_profile_add_goal) {
            Intent intent = new Intent(getActivity(), AddGoalActivity.class);
            startActivityForResult(intent, Constants.ADD_GOAL_ACTIVITY_CODE);
        } else if (id == android.R.id.home) {
            getActivity().onBackPressed();
        } else if (id == R.id.menu_doctor_profile_patient_settings) {
            ((BaseActivity) getActivity()).showSnackbarWithDuration(R.string.not_implemented_yet, Snackbar.LENGTH_SHORT);
        }
        return true;
    }


    private void setupViewPager(ViewPager viewPager) {
        mTabsAdapter.addFragment(DoctorHomeProfileFragment.newInstance(), getString(R.string.home_doctor_profile_title));
        mTabsAdapter.addFragment(DoctorHomeAlertListFragment.newInstance(), getString(R.string.home_doctor_alerts_title));
        viewPager.setAdapter(mTabsAdapter);
        mTabLayout.setupWithViewPager(mTabsViewPager);

        RelativeLayout myAlertsTabLayout = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(R.layout.tab_badge_layout, null);
        TabLayout.Tab tabAlerts = mTabLayout.getTabAt(1);
        if (tabAlerts != null) {
            tabAlerts.setCustomView(myAlertsTabLayout);
        }
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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.doctor_profile_menu, menu);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDoctorHomeViewModel = ViewModelProviders.of(this, mViewModelFactory).get(DoctorHomeViewModel.class);
        mMainActivityViewModel = ViewModelProviders.of(getActivity()).get(MainActivityViewModel.class);
    }

    @Override
    public MyTabAdapter getPagerAdapter() {
        return mTabsAdapter;
    }
}
