package com.dglozano.escale.ui.main.diet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.dglozano.escale.R;
import com.dglozano.escale.ui.doctor.main.AddDietActivity;
import com.dglozano.escale.ui.main.MainActivityViewModel;
import com.dglozano.escale.ui.main.diet.all.AllDietsFragment;
import com.dglozano.escale.ui.main.diet.current.CurrentDietFragment;
import com.dglozano.escale.util.Constants;
import com.dglozano.escale.util.ui.MyTabAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import butterknife.OnClick;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import timber.log.Timber;

public class DietFragment extends Fragment {

    @BindView(R.id.diet_view_pager_tabs)
    ViewPager mTabsViewPager;
    @BindView(R.id.diets_current_or_all_tablayout)
    TabLayout mTabLayout;
    @BindView(R.id.no_diets_layout)
    RelativeLayout mNoDietsLayout;
    @BindView(R.id.diets_main_container)
    RelativeLayout mDietsMainContainer;
    @BindView(R.id.add_diet_btn)
    FloatingActionButton addDietBtn;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    @Inject
    MyTabAdapter mTabsAdapter;

    private Unbinder mViewUnbinder;
    private MainActivityViewModel mMainActivityViewModel;
    private DietViewModel mDietViewModel;

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

        mDietViewModel.areDietsEmpty().observe(this, dietEmpty -> {
            if (dietEmpty != null && !dietEmpty) {
                mDietsMainContainer.setVisibility(View.VISIBLE);
                mTabLayout.setVisibility(View.VISIBLE);
                mNoDietsLayout.setVisibility(View.GONE);
                mTabLayout.setupWithViewPager(mTabsViewPager);
            } else {
                mNoDietsLayout.setVisibility(View.VISIBLE);
                mDietsMainContainer.setVisibility(View.GONE);
                mTabLayout.setVisibility(View.GONE);
            }
        });

        addDietBtn.setVisibility(mMainActivityViewModel.isDoctorView() ? View.VISIBLE : View.GONE);

        return view;
    }

    private void setupViewPager(ViewPager viewPager) {
        mTabsAdapter.addFragment(new CurrentDietFragment(), getString(R.string.current_diet_title));
        mTabsAdapter.addFragment(new AllDietsFragment(), getString(R.string.all_diets_title));
        viewPager.setAdapter(mTabsAdapter);
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

    @OnClick(R.id.add_diet_btn)
    public void onAddDietClick(View v) {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, getString(R.string.diet_file_chooser_intent_title)),
                    Constants.SELECT_DIET_FILE_TO_ADD);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Timber.e("No file chooser available. Download one");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.SELECT_DIET_FILE_TO_ADD
                && data != null && data.getData() != null) {
            Uri dietFileUri = data.getData();
            Intent intent = new Intent(getActivity(), AddDietActivity.class);
            intent.putExtra(Constants.DIET_FILE_URI, dietFileUri);
            startActivityForResult(intent, Constants.ADD_DIET_ACTIVITY_CODE);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDietViewModel = ViewModelProviders.of(this, mViewModelFactory).get(DietViewModel.class);
        mMainActivityViewModel = ViewModelProviders.of(getActivity()).get(MainActivityViewModel.class);
    }
}
