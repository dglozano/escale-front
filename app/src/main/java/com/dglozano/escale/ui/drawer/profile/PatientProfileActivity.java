package com.dglozano.escale.ui.drawer.profile;

import android.Manifest;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dglozano.escale.R;
import com.dglozano.escale.db.entity.Doctor;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.ui.BaseActivity;
import com.dglozano.escale.util.FileUtils;
import com.dglozano.escale.util.PermissionHelper;
import com.dglozano.escale.util.ui.Event;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import timber.log.Timber;

public class PatientProfileActivity extends BaseActivity implements EditPatientDialog.EditPatientDialogListener {

    private static final int SELECT_PICTURE_CODE = 789;

    @BindView(R.id.profile_progress_bar_container)
    RelativeLayout mProgressBarContainer;
    @BindView(R.id.profile_user_picture)
    RoundedImageView mProfilePicture;
    @BindView(R.id.profile_pic_progress_bar)
    ProgressBar mProfilePicProgressBar;
    @BindView(R.id.profile_user_name)
    TextView mUserName;
    @BindView(R.id.profile_user_email)
    TextView mUserEmail;
    @BindView(R.id.profile_user_doctor_name)
    TextView mDoctorName;
    @BindView(R.id.profile_user_age)
    TextView mAge;
    @BindView(R.id.profile_user_activity)
    TextView mPhysicalActivity;
    @BindView(R.id.profile_user_height)
    TextView mHeight;
    @BindView(R.id.profile_root)
    ConstraintLayout mProfileInfoGroup;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    @Inject
    UCrop.Options uCropOptions;
    @Inject
    Picasso mPicasso;
    @Inject
    AlphaAnimation mFadeAnimation;

    private EditPatientDialog mEditPatientDialog = null;
    private PatientProfileActivityViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(PatientProfileActivityViewModel.class);

        ButterKnife.bind(this);

        mProfileInfoGroup.setAnimation(mFadeAnimation);

        mViewModel.getLoading().observe(this, this::onLoadingStateChange);
        mViewModel.getErrorEvent().observe(this, this::onErrorEventFired);
        mViewModel.getSuccessEvent().observe(this, this::onSuccessEventFired);

        mProfilePicture.setOnClickListener(this::onProfilePictureClick);

        mViewModel.getLoggedPatient().observe(this, this::updatePatientData);
        mViewModel.getDoctorOfLoggedPatient().observe(this, this::updateDoctorData);
    }

    private void updateDoctorData(Doctor doctor) {
        if (doctor != null) {
            mDoctorName.setText(String.format("Dr. %s %s", doctor.getFirstName(), doctor.getLastName()));
            mDoctorName.animate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_profile_edit:
                showEditDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showEditDialog() {
        mEditPatientDialog = EditPatientDialog
                .newInstance(mViewModel.getPatientHeight(), mViewModel.getPatientActivity());
        mEditPatientDialog.show(getSupportFragmentManager(), "showEditPatientDialog");
    }

    private void updatePatientData(Patient patient) {
        if (patient != null) {
            loadPicture();
            mUserName.setText(String.format("%s %s", patient.getFirstName(), patient.getLastName()));
            mUserEmail.setText(patient.getEmail());
            mAge.setText(String.format("%s años", patient.getAge()));
            mHeight.setText(String.format("%s cm.", patient.getHeightInCm()));
            mPhysicalActivity.setText(patient.getActivityString());
            mProfileInfoGroup.setVisibility(View.VISIBLE);
            mProfileInfoGroup.animate();
        }
    }

    private void loadPicture() {
        try {
            mProfilePicProgressBar.setVisibility(View.VISIBLE);
            mPicasso.load(mViewModel.getProfileImageUrlOfLoggedPatient().toString())
                    .placeholder(R.color.almostWhite)
                    .noFade()
                    .error(R.drawable.ic_user_profile_image_default)
                    .into(mProfilePicture, new Callback() {
                        @Override
                        public void onSuccess() {
                            mProfilePicProgressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            mProfilePicProgressBar.setVisibility(View.GONE);
                        }
                    });
        } catch (MalformedURLException e) {
            Timber.e(e);
        }
    }

    private void onSuccessEventFired(Event<Integer> successEvent) {
        if (successEvent != null && !successEvent.hasBeenHandled()) {
            Integer successMessageResId = successEvent.handleContent();
            boolean isPictureChangedEvent = successMessageResId == R.string.upload_picture_success_msg;
            if(isPictureChangedEvent) {
                try {
                    mPicasso.invalidate(Uri.parse(mViewModel.getProfileImageUrlOfLoggedPatient().toString()));
                    loadPicture();
                } catch (MalformedURLException e) {
                    Timber.e(e);
                }
            }
            showSnackbarWithDuration(successMessageResId, Snackbar.LENGTH_SHORT);
        }
    }

    private void onErrorEventFired(Event<Integer> errorEvent) {
        if (errorEvent != null && !errorEvent.hasBeenHandled()) {
            showSnackbarWithOkDismiss(errorEvent.handleContent());
        }
    }

    private void onLoadingStateChange(Boolean isLoading) {
        if (isLoading != null && isLoading) {
            mProgressBarContainer.setVisibility(View.VISIBLE);
        } else {
            mProgressBarContainer.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_PICTURE_CODE && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            String randomTempName = UUID.randomUUID().toString() + ".jpeg";
            Uri tempUri = Uri.fromFile(new File(getCacheDir(), randomTempName));
            UCrop.of(imageUri, tempUri)
                    .withOptions(uCropOptions)
                    .withAspectRatio(1, 1)
                    .start(this);
        } else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri pictureUri = UCrop.getOutput(data);
            if (pictureUri == null) {
                showSnackbarWithDuration(R.string.upload_picture_error_msg, Snackbar.LENGTH_SHORT);
            } else {
                String mediaType = getMimeType(pictureUri);
                if (mediaType == null) {
                    showSnackbarWithDuration(R.string.upload_picture_error_msg, Snackbar.LENGTH_SHORT);
                } else {
                    File picture = FileUtils.getFile(this, pictureUri);
                    mViewModel.hitUploadPicture(picture, mediaType);
                }
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Timber.e(UCrop.getError(data));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (PermissionHelper.isPermissionGranted(requestCode, permissions, grantResults, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Timber.d("Permission granted. Starting Image picker intent...");
            pickImage();
        }
    }

    private String getMimeType(Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    private void pickImage() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");
        getIntent.addCategory(Intent.CATEGORY_OPENABLE);
        Intent chooserIntent = Intent.createChooser(getIntent, "Seleccionar foto");
        startActivityForResult(chooserIntent, SELECT_PICTURE_CODE);
    }

    public void onProfilePictureClick(View v) {
        if (PermissionHelper.isPermissionGranted(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Timber.d("Clicked on picture. Permission already granted...");
            pickImage();
        } else {
            Timber.d("Doesn't have Permission. Asking for it...");
            PermissionHelper.requestExternalStoragePermission(this);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected View getRootLayout() {
        return findViewById(android.R.id.content);
    }

    @Override
    public void onEditSubmit() {
        int newHeight = mEditPatientDialog.getInputHeight();
        int newActivity = mEditPatientDialog.getInputActivity();

        Timber.d("New height: %s - New Activity: %s", newHeight, newActivity);

        mViewModel.hitUpdateUserHeightAndActivity(newHeight, newActivity);
    }
}
