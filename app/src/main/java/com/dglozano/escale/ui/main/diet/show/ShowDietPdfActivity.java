package com.dglozano.escale.ui.main.diet.show;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.dglozano.escale.R;
import com.dglozano.escale.util.Constants;
import com.dglozano.escale.util.ui.CustomPdfScrollHandle;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;

import java.io.File;
import java.util.Objects;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
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
        Uri fileUri = intent.getParcelableExtra(Constants.DIET_FILE_URI);

        pdfView.fromUri(fileUri)
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
