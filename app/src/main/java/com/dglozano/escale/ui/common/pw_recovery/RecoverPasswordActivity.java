package com.dglozano.escale.ui.common.pw_recovery;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.dglozano.escale.R;
import com.dglozano.escale.databinding.ActivityRecoverPasswordBinding;
import com.dglozano.escale.ui.BaseActivity;
import com.dglozano.escale.util.ui.Event;

import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.AndroidInjection;

public class RecoverPasswordActivity extends BaseActivity {

    @BindView(R.id.recover_password_progress_bar_container)
    RelativeLayout mProgressBarContainer;
    @BindView(R.id.recover_email_edittext)
    EditText mEmail;

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

    private void onSuccessEventFired(Event<String> successEvent) {
        if(successEvent != null && !successEvent.hasBeenHandled()) {
            Intent intent = getIntent();
            intent.putExtra("email", successEvent.handleContent());
            setResult(RESULT_OK, intent);
            finish();
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
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @OnClick(R.id.recover_password_btn)
    public void recoverPassword() {
        mViewModel.hitRecoverPassword(mEmail.getText().toString());
    }

    @Override
    protected View getRootLayout() {
        return findViewById(android.R.id.content);
    }
}
