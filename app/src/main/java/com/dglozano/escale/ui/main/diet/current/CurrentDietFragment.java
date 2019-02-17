package com.dglozano.escale.ui.main.diet.current;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.dglozano.escale.R;
import com.github.barteksc.pdfviewer.PDFView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import timber.log.Timber;

public class CurrentDietFragment extends Fragment {
    @BindView(R.id.actual_diet_pdf_view)
    PDFView mPdfView;
    @BindView(R.id.no_current_diet_layout)
    RelativeLayout mNoCurrentDietLayout;
    @BindView(R.id.current_diet_swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private Unbinder mViewUnbinder;
    private CurrentDietViewModel mViewModel;

    public CurrentDietFragment() {
        // Required empty public constructor
    }

    public static CurrentDietFragment newInstance() {
        return new CurrentDietFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.sub_fragment_actual_diet, container, false);
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
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(CurrentDietViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mViewModel.getCurrentDiet().observe(this, currentDiet -> {
            if (currentDiet == null) {
                mPdfView.setVisibility(View.GONE);
                mNoCurrentDietLayout.setVisibility(View.VISIBLE);
            } else {
                mNoCurrentDietLayout.setVisibility(View.GONE);
                mPdfView.setVisibility(View.VISIBLE);
            }
        });
        mViewModel.getRefreshingListStatus().observe(this, isRefreshing -> {
            mSwipeRefreshLayout.setRefreshing(isRefreshing != null && isRefreshing);
        });
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mViewModel.refreshCurrentDiet();
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.d("onDestroyView().");
        mViewUnbinder.unbind();
    }
}
