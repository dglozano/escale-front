package com.dglozano.escale.ui.common;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.RelativeLayout;

import com.dglozano.escale.R;
import com.dglozano.escale.databinding.ActivityChangePasswordBinding;
import com.dglozano.escale.web.EscaleRestApi;

import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.AndroidInjection;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class ChangePasswordActivity extends BaseActivity {

    @BindView(R.id.change_password_progress_bar_container)
    RelativeLayout mProgressBarContainer;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    @Inject
    EscaleRestApi escaleRestApi;

    private boolean isForcedToChangePassword;
    private ChangePasswordActivityViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(ChangePasswordActivityViewModel.class);

        // Inflate view and obtain an instance of the binding class.
        ActivityChangePasswordBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_change_password);

        // Assign the component to a property in the binding class.
        binding.setViewmodel(mViewModel);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        int userId = intent.getIntExtra("user_id", -1);
        isForcedToChangePassword = intent.getBooleanExtra("forced_to_change_pass", false);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        if (userId == -1) {
            finish();
        } else {
            mViewModel.setUserId(userId);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (isForcedToChangePassword) {
            showYouMustChangePasswordDialog();
            return false;
        } else {
            finish();
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        if (isForcedToChangePassword) {
            showYouMustChangePasswordDialog();
        } else {
            finish();
        }
    }

    private void showYouMustChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_NoActionBar);
        builder.setMessage(getString(R.string.must_change_password_error_msg_dialog))
                .setNeutralButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                .show();
    }

    @OnClick(R.id.change_password_btn)
    public void changePassword() {
        showProgressDialog();
        Call<Void> changePasswordRseponseCall = escaleRestApi.changePassword(
                mViewModel.getChangePasswordData(),
                mViewModel.getUserId());
        changePasswordRseponseCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.code() == 200) {
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    hideProgressDialog();
                    Timber.d("Change password error - Response code is not 200 (Error)");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                hideProgressDialog();
                Timber.d(t, "Change password error - OnFailure");
            }
        });
    }

    private void hideProgressDialog() {
        if (mProgressBarContainer.getVisibility() == View.VISIBLE) {
            mProgressBarContainer.setVisibility(View.GONE);
        }
    }

    private void showProgressDialog() {
        mProgressBarContainer.setVisibility(View.VISIBLE);
    }

    @Override
    protected View getRootLayout() {
        return findViewById(android.R.id.content);
    }
}
