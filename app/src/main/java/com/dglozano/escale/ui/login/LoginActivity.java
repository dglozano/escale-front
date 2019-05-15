package com.dglozano.escale.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.dglozano.escale.R;
import com.dglozano.escale.databinding.ActivityLoginBinding;
import com.dglozano.escale.ui.BaseActivity;
import com.dglozano.escale.ui.common.pw_recovery.RecoverPasswordActivity;
import com.dglozano.escale.ui.doctor.main.DoctorMainActivity;
import com.dglozano.escale.ui.main.MainActivity;
import com.dglozano.escale.util.Constants;
import com.dglozano.escale.util.ui.Event;
import com.dglozano.escale.web.EscaleRestApi;

import javax.inject.Inject;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.AndroidInjection;
import timber.log.Timber;

public class LoginActivity extends BaseActivity {

    private static final int RECOVER_PASSWORD_CODE = 444;
    @BindView(R.id.login_root)
    View mRootView;
    @BindView(R.id.login_progress_bar_container)
    RelativeLayout mProgressBarContainer;
    @BindView(R.id.login_password)
    EditText mPasswordEditText;
    @BindView(R.id.login_email)
    EditText mEmailEditText;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    @Inject
    SharedPreferences sharedPreferences;
    @Inject
    EscaleRestApi escaleRestApi;

    private LoginActivityViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        // Enable transitions
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(LoginActivityViewModel.class);

        // Inflate view and obtain an instance of the binding class.
        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        // Assign the component to a property in the binding class.
        binding.setViewmodel(mViewModel);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean(Constants.ASK_NEW_FIREBASE_TOKEN, false)) {
            mViewModel.askForNewFirebaseToken();
        }

        ButterKnife.bind(this);
        mViewModel.getPatientIdChangedEvent().observe(this, this::onLoggedPatientChanged);
        mViewModel.getDoctorIdChangedEvent().observe(this, this::onLoggedDoctorChanged);

        mViewModel.getLoading().observe(this, this::onLoadingStateChange);
        mViewModel.getErrorEvent().observe(this, this::onErrorEventFired);
    }

    @OnClick(R.id.login_sign_in_button)
    public void signIn() {
        mViewModel.hitLogin(mEmailEditText.getText().toString(), mPasswordEditText.getText().toString());
    }

    @OnClick(R.id.login_reset_password)
    public void recoverPassword() {
        Intent intent = new Intent(this, RecoverPasswordActivity.class);
        startActivityForResult(intent, RECOVER_PASSWORD_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECOVER_PASSWORD_CODE && resultCode == RESULT_OK) {
            String email = data.getStringExtra("email");
            if (email == null || email.isEmpty()) email = "tu cuenta";
            showSnackbarWithOkDismiss(String.format(getString(R.string.email_sent_to_recover_snak_msg), email));
        }
    }

    @Override
    protected View getRootLayout() {
        return findViewById(android.R.id.content);
    }

    private void onLoggedPatientChanged(Event<Long> idEvent) {
        Timber.d("Id onLoggedPatientChanged %s", idEvent.peekContent());
        if (!idEvent.hasBeenHandled() && idEvent.handleContent() != -1L && mViewModel.getLoggedDoctorId() == -1L) {
            startMainActivity();
        }
    }

    private void onLoggedDoctorChanged(Event<Long> idEvent) {
        Timber.d("Id onLoggedDoctorChanged %s", idEvent.peekContent());
        if (!idEvent.hasBeenHandled() && idEvent.handleContent() != -1L) {
            startDoctorMainActivity();
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void startDoctorMainActivity() {
        Intent intent = new Intent(this, DoctorMainActivity.class);
        startActivity(intent);
        finish();
    }

    private void onLoadingStateChange(Boolean isLoading) {
        if (isLoading != null && isLoading) {
            mProgressBarContainer.setVisibility(View.VISIBLE);
        } else {
            mProgressBarContainer.setVisibility(View.GONE);
        }
    }
}
