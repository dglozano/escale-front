package com.dglozano.escale.ui.main.diet;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dglozano.escale.R;
import com.dglozano.escale.ui.main.MainActivity;
import com.dglozano.escale.ui.main.MainActivityViewModel;
import com.dglozano.escale.ui.main.diet.current.CurrentDietFragment;
import com.dglozano.escale.ui.main.diet.old.OldDietsFragment;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import timber.log.Timber;


public class DietFragment extends Fragment {

    @BindView(R.id.diet_view_pager_tabs)
    ViewPager mTabsViewPager;
    @BindView(R.id.diets_current_or_all_tablayout)
    TabLayout mTabLayout;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    @Inject
    DietTabAdapter mTabsAdapter;

    private Unbinder mViewUnbinder;
    private MainActivityViewModel mMainActivityViewModel;

    private boolean started = false;
    private boolean visible = false;

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

        setupViewPager(mTabsViewPager);
        mTabLayout.setupWithViewPager(mTabsViewPager);
        return view;
    }

    private void setupViewPager(ViewPager viewPager) {
        mTabsAdapter.addFragment(new CurrentDietFragment(), getString(R.string.current_diet_title));
        mTabsAdapter.addFragment(new OldDietsFragment(), getString(R.string.old_diets_title));
        viewPager.setAdapter(mTabsAdapter);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        this.visible = isVisibleToUser;

        if(this.started) {
            mMainActivityViewModel.toogleAppBarShadow(!this.visible);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.started = false;
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
        this.started = true;
        mMainActivityViewModel = ViewModelProviders.of((MainActivity) context).get(MainActivityViewModel.class);
        mMainActivityViewModel.toogleAppBarShadow(!this.visible);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate().");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.d("onDestroyView().");
        mViewUnbinder.unbind();
    }
}
