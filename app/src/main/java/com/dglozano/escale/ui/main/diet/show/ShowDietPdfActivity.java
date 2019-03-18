package com.dglozano.escale.ui.main.diet.show;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.dglozano.escale.R;
import com.dglozano.escale.util.ui.CustomPdfScrollHandle;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;

import java.io.File;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;

public class ShowDietPdfActivity extends AppCompatActivity {

    @BindView(R.id.show_diet_pdf_view)
    PDFView pdfView;

    @Inject
    CustomPdfScrollHandle mScrollHandle;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private ShowDietPdfActivityViewModel mViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_diet_pdf_view);
        ButterKnife.bind(this);

        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(ShowDietPdfActivityViewModel.class);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String filepath = intent.getStringExtra("diet_file_path");
        File pdf = new File(filepath);

        pdfView.fromFile(pdf)
                .scrollHandle(mScrollHandle)
                .pageSnap(true)
                .autoSpacing(true)
                .pageFling(true)
                .pageFitPolicy(FitPolicy.WIDTH)
                .load();
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(Activity.RESULT_OK);
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        finish();
    }
}
