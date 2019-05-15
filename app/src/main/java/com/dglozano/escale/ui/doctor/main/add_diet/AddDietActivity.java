package com.dglozano.escale.ui.doctor.main.add_diet;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.dglozano.escale.R;
import com.dglozano.escale.ui.BaseActivity;
import com.dglozano.escale.ui.main.diet.show.ShowDietPdfActivity;
import com.dglozano.escale.util.Constants;
import com.dglozano.escale.util.MyFileUtils;
import com.dglozano.escale.util.ValidationHelper;
import com.dglozano.escale.util.ui.CustomPdfScrollHandle;
import com.dglozano.escale.util.ui.Event;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.google.android.material.textfield.TextInputLayout;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import dagger.android.AndroidInjection;
import timber.log.Timber;

import static com.dglozano.escale.util.Constants.SHOW_PDF_CODE;
import static com.dglozano.escale.util.MyFileUtils.getMimeType;

public class AddDietActivity extends BaseActivity {

    @BindView(R.id.add_diet_pdf_preview)
    PDFView pdfView;
    @BindView(R.id.add_diet_filename_inputlayout)
    TextInputLayout mDietFileNameInputLayout;
    @BindView(R.id.add_diet_filename_edittext)
    EditText mDietNameEditText;
    @BindView(R.id.diet_progress_bar_container)
    RelativeLayout mProgressBarContainer;

    @Inject
    CustomPdfScrollHandle mScrollHandle;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private AddDietActivityViewModel mViewModel;

    @Override
    protected View getRootLayout() {
        return findViewById(android.R.id.content);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_diet);
        ButterKnife.bind(this);

        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(AddDietActivityViewModel.class);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        Uri fileUri = intent.getParcelableExtra(Constants.DIET_FILE_URI);
        String filename = MyFileUtils.getFileName(this, fileUri);

        mDietNameEditText.setText(filename);
        mViewModel.setDietFileUri(fileUri);

        pdfView.fromUri(fileUri)
                .scrollHandle(mScrollHandle)
                .pageSnap(true)
                .autoSpacing(true)
                .pageFling(true)
                .pageFitPolicy(FitPolicy.WIDTH)
                .load();

        mViewModel.getLoading().observe(this, this::onLoadingStateChange);
        mViewModel.getErrorEvent().observe(this, this::onErrorEventFired);
        mViewModel.getSuccessEvent().observe(this, this::onSuccessEventFired);
    }

    @Override
    public void onErrorEventFired(Event<Integer> errorEvent) {
        super.onErrorEventFired(errorEvent);
        setErrorInInputLayout(getDietInputErrorStringResource(mDietNameEditText.getText()),
                mDietFileNameInputLayout);
    }

    private void onLoadingStateChange(Boolean isLoading) {
        if (isLoading != null && isLoading) {
            mProgressBarContainer.setVisibility(View.VISIBLE);
        } else {
            mProgressBarContainer.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.add_diet_btn)
    public void onAddDietSubmit(View v) {
        String filename = mDietNameEditText.getText().toString();
        Uri dietUri = mViewModel.getDietFileUri();
        if (dietUri == null) {
            Timber.e("dietUri is null");
            showErrorSnackbarWithOkDismiss(R.string.upload_diet_error_msg);
        } else {
            String mediaType = getMimeType(dietUri, getContentResolver());
            if (mediaType == null) {
                Timber.e("mediaType is null");
                showErrorSnackbarWithOkDismiss(R.string.upload_diet_error_msg);
            } else {
                InputStream dietInputStream = MyFileUtils.getFileInputStream(this, dietUri);
                String randomTempName = UUID.randomUUID().toString() + ".pdf";
                File dietFile = new File(getCacheDir(), randomTempName);
                try {
                    FileUtils.copyInputStreamToFile(dietInputStream, dietFile);
                } catch (IOException e) {
                    Timber.e(e);
                    showErrorSnackbarWithOkDismiss(R.string.upload_diet_error_msg);
                }

                mViewModel.hitUploadDiet(dietFile, mediaType, filename);
            }
        }
    }

    @OnClick(R.id.add_diet_change_btn)
    public void onChangeDietClick(View v) {
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
            Uri fileUri = data.getData();

            String filename = MyFileUtils.getFileName(this, fileUri);

            mDietNameEditText.setText(filename);
            mViewModel.setDietFileUri(fileUri);

            pdfView.fromUri(fileUri)
                    .scrollHandle(mScrollHandle)
                    .pageSnap(true)
                    .autoSpacing(true)
                    .pageFling(true)
                    .pageFitPolicy(FitPolicy.WIDTH)
                    .load();

        }
    }

    @OnClick(R.id.add_diet_fullsize_btn)
    public void onFullScreenDietClick(View v) {
        Intent intent = new Intent(this, ShowDietPdfActivity.class);
        intent.putExtra(Constants.DIET_FILE_URI, mViewModel.getDietFileUri());
        startActivityForResult(intent, SHOW_PDF_CODE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(Activity.RESULT_CANCELED);
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @OnTextChanged(value = R.id.add_diet_filename_edittext,
            callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterEditTextChangedDietName(Editable editable) {
        setErrorInInputLayout(getDietInputErrorStringResource(editable), mDietFileNameInputLayout);
    }

    @Nullable
    private Integer getDietInputErrorStringResource(Editable editable) {
        Integer errorString;
        if (editable.toString().length() > mDietFileNameInputLayout.getCounterMaxLength()) {
            errorString = R.string.diet_name_too_long;
        } else if (TextUtils.isEmpty(editable)) {
            errorString = R.string.input_validation_empty_error;
        } else {
            errorString = ValidationHelper.isValidFileName(editable) ? null : R.string.input_validation_diet_name;
        }
        return errorString;
    }
}
