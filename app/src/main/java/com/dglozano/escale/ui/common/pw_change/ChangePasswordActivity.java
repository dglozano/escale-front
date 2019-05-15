package com.dglozano.escale.ui.common.pw_change;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.dglozano.escale.R;
import com.dglozano.escale.databinding.ActivityChangePasswordBinding;
import com.dglozano.escale.ui.BaseActivity;
import com.dglozano.escale.util.Constants;
import com.dglozano.escale.util.ui.Event;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import dagger.android.AndroidInjection;

import static com.dglozano.escale.util.ValidationHelper.getPasswordError;

public class ChangePasswordActivity extends BaseActivity {

    @BindView(R.id.change_password_progress_bar_container)
    RelativeLayout mProgressBarContainer;
    @BindView(R.id.current_password_edittext)
    EditText mCurrentPasswordEditText;
    @BindView(R.id.new_password_edittext)
    EditText mNewPasswordEditText;
    @BindView(R.id.new_password_repeat_edittext)
    EditText mNewPasswordRepeatEditText;
    @BindView(R.id.current_password_inputlayout)
    TextInputLayout mCurrentPasswordInputLayout;
    @BindView(R.id.new_password_inputlayout)
    TextInputLayout mNewPasswordInputLayout;
    @BindView(R.id.new_password_repeat_inputlayout)
    TextInputLayout mNewPasswordRepeatInputLayout;

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
        mViewModel.setUserId(intent.getLongExtra(Constants.CHANGE_PW_USER_ID_EXTRA, -1L));
        isForcedToChangePassword = intent.getBooleanExtra("forced_to_change_pass", false);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mViewModel.getLoading().observe(this, this::onLoadingStateChange);
        mViewModel.getErrorEvent().observe(this, this::onErrorEventFired);
        mViewModel.getSuccessEvent().observe(this, this::onSuccessEventFired);
    }

    @Override
    public void onErrorEventFired(Event<Integer> errorEvent) {
        super.onErrorEventFired(errorEvent);
        validateCurrentPassword(mCurrentPasswordEditText.getText());
        validateNewPassword(mNewPasswordEditText.getText().toString(), mNewPasswordInputLayout, mNewPasswordRepeatInputLayout);
        validateNewPassword(mNewPasswordRepeatEditText.getText().toString(), mNewPasswordRepeatInputLayout, mNewPasswordInputLayout);
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
                mCurrentPasswordEditText.getText().toString(),
                mNewPasswordEditText.getText().toString(),
                mNewPasswordRepeatEditText.getText().toString()
        );
    }

    @OnFocusChange(R.id.current_password_edittext)
    public void onFocusChangeCurrentPassword(View v, boolean hasFocus) {
        if (!hasFocus) {
            validateCurrentPassword(((EditText) v).getText());
        }
    }

    @OnTextChanged(value = R.id.current_password_edittext,
            callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void afterEditTextCurrentPassword(Editable editable) {
        validateCurrentPassword(editable);
    }

    @OnFocusChange(R.id.new_password_edittext)
    public void onFocusChangeNewPassword(View v, boolean hasFocus) {
        if (!hasFocus) {
            validateNewPassword(((EditText) v).getText(),
                    mNewPasswordInputLayout,
                    mNewPasswordRepeatInputLayout);
        }
    }

    @OnTextChanged(value = R.id.new_password_edittext,
            callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void afterEditTextNewPassword(Editable editable) {
        validateNewPassword(editable,
                mNewPasswordInputLayout,
                mNewPasswordRepeatInputLayout);
    }

    @OnFocusChange(R.id.new_password_repeat_edittext)
    public void onFocusChangeNewPasswordRepeat(View v, boolean hasFocus) {
        if (!hasFocus) {
            validateNewPassword(((EditText) v).getText(),
                    mNewPasswordRepeatInputLayout,
                    mNewPasswordInputLayout);
        }
    }

    @OnTextChanged(value = R.id.new_password_repeat_edittext,
            callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void afterEditTextNewPasswordRepeat(Editable editable) {
        validateNewPassword(editable,
                mNewPasswordRepeatInputLayout,
                mNewPasswordInputLayout);
    }

    private void validateNewPassword(CharSequence password, TextInputLayout inputToSetError, TextInputLayout otherInput) {
        Integer error = getPasswordError(password);
        if (error == null) {
            if (mCurrentPasswordEditText.getText().toString()
                    .equals(mNewPasswordEditText.getText().toString())) {
                error = R.string.password_validation_same_password_error;
            } else if (!TextUtils.isEmpty(otherInput.getEditText().getText())
                    && !mNewPasswordEditText.getText().toString().equals(mNewPasswordRepeatEditText.getText().toString())) {
                error = R.string.password_validation_password_mismatch_error;
            } else if (otherInput.getError() != null
                    && otherInput.getError()
                    .equals(getString(R.string.password_validation_password_mismatch_error))) {
                otherInput.setError(null);
            }
        }
        setErrorInInputLayout(error, inputToSetError);
    }

    private void validateCurrentPassword(CharSequence currentPassword) {
        Integer error = getPasswordError(currentPassword);
        if (mNewPasswordRepeatInputLayout.getError() != null
                && mNewPasswordRepeatInputLayout.getError()
                .equals(getString(R.string.password_validation_same_password_error))) {
            mNewPasswordRepeatInputLayout.setError(null);
        }
        if (mNewPasswordInputLayout.getError() != null
                && mNewPasswordInputLayout.getError()
                .equals(getString(R.string.password_validation_same_password_error))) {
            mNewPasswordInputLayout.setError(null);
        }
        setErrorInInputLayout(error, mCurrentPasswordInputLayout);
    }

    @Override
    protected View getRootLayout() {
        return findViewById(android.R.id.content);
    }
}
