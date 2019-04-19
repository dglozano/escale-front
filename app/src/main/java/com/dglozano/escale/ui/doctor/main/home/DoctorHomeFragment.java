package com.dglozano.escale.ui.doctor.main.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dglozano.escale.R;
import com.dglozano.escale.ui.doctor.main.home.alerts.DoctorHomeAlertListFragment;
import com.dglozano.escale.ui.doctor.main.home.profile.DoctorHomeProfileFragment;
import com.dglozano.escale.ui.main.MainActivityViewModel;
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

public class DoctorHomeFragment extends Fragment {

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

        return view;
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDoctorHomeViewModel = ViewModelProviders.of(this, mViewModelFactory).get(DoctorHomeViewModel.class);
        mMainActivityViewModel = ViewModelProviders.of(getActivity()).get(MainActivityViewModel.class);
    }
}
