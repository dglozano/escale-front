package com.dglozano.escale.ui.common.pw_change;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.dglozano.escale.R;
import com.dglozano.escale.databinding.ActivityChangePasswordBinding;
import com.dglozano.escale.ui.BaseActivity;
import com.dglozano.escale.util.ui.Event;

import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.AndroidInjection;

public class ChangePasswordActivity extends BaseActivity {

    @BindView(R.id.change_password_progress_bar_container)
    RelativeLayout mProgressBarContainer;
    @BindView(R.id.current_password_input)
    EditText mCurrentPasswordInput;
    @BindView(R.id.new_password_input)
    EditText mNewPasswordEditInput;
    @BindView(R.id.new_password_repeat_input)
    EditText mNewPasswordRepeatInput;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

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
        isForcedToChangePassword = intent.getBooleanExtra("forced_to_change_pass", false);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mViewModel.getLoading().observe(this, this::onLoadingStateChange);
        mViewModel.getErrorEvent().observe(this, this::onErrorEventFired);
        mViewModel.getSuccessEvent().observe(this, this::onSuccessEventFired);
    }

    private void onSuccessEventFired(Event<Long> successEvent) {
        setResult(Activity.RESULT_OK);
        finish();
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
        if (isForcedToChangePassword) showYouMustChangePasswordDialog();
        else finish();
    }

    private void showYouMustChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_NoActionBar);
        builder.setTitle(getString(R.string.dialog_title_error))
                .setMessage(getString(R.string.must_change_password_error_msg_dialog))
                .setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss())
                .show();
    }

    @OnClick(R.id.change_password_btn)
    public void changePassword() {
        mViewModel.hitChangePassword(
                mCurrentPasswordInput.getText().toString(),
                mNewPasswordEditInput.getText().toString(),
                mNewPasswordRepeatInput.getText().toString()
        );
    }

    @Override
    protected View getRootLayout() {
        return findViewById(android.R.id.content);
    }
}
