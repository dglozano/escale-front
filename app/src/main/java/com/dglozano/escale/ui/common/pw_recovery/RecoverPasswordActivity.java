package com.dglozano.escale.ui.common.pw_recovery;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.dglozano.escale.R;
import com.dglozano.escale.databinding.ActivityRecoverPasswordBinding;
import com.dglozano.escale.ui.BaseActivity;
import com.dglozano.escale.util.ui.Event;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import javax.inject.Inject;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import dagger.android.AndroidInjection;

import static com.dglozano.escale.util.ValidationHelper.getEmailError;

public class RecoverPasswordActivity extends BaseActivity {

    @BindView(R.id.recover_password_progress_bar_container)
    RelativeLayout mProgressBarContainer;
    @BindView(R.id.recover_password_email_edittext)
    EditText mEmailEditText;
    @BindView(R.id.recover_password_email_inputlayout)
    TextInputLayout mEmailInputLayout;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private RecoverPasswordActivityViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(RecoverPasswordActivityViewModel.class);

        // Inflate view and obtain an instance of the binding class.
        ActivityRecoverPasswordBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_recover_password);

        // Assign the component to a property in the binding class.
        binding.setViewmodel(mViewModel);

        ButterKnife.bind(this);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mViewModel.getLoading().observe(this, this::onLoadingStateChange);
        mViewModel.getErrorEvent().observe(this, this::onErrorEventFired);
        mViewModel.getSuccessEvent().observe(this, this::onSuccessEventFired);
    }

    @Override
    public void onSuccessEventFired(Event<?> successEvent) {
        if (successEvent != null && !successEvent.hasBeenHandled()) {
            Intent intent = getIntent();
            intent.putExtra("email", successEvent.handleContent().toString());
            setResult(RESULT_OK, intent);
            finish();
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
    public void onErrorEventFired(Event<Integer> errorEvent) {
        super.onErrorEventFired(errorEvent);
        setErrorInInputLayout(getEmailError(mEmailEditText.getText()), mEmailInputLayout);
    }

    @OnTextChanged(value = R.id.recover_password_email_edittext,
            callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void afterEditTextEmail(Editable editable) {
        setErrorInInputLayout(getEmailError(editable), mEmailInputLayout);
    }

    @OnFocusChange(R.id.recover_password_email_edittext)
    public void onFocusChangeEmail(View v, boolean hasFocus) {
        if (!hasFocus) {
            setErrorInInputLayout(getEmailError(((EditText) v).getText()), mEmailInputLayout);
        }
    }

    @OnClick(R.id.recover_password_btn)
    public void recoverPassword() {
        mViewModel.hitRecoverPassword(mEmailEditText.getText().toString());
    }

    @Override
    protected View getRootLayout() {
        return findViewById(android.R.id.content);
    }
}
