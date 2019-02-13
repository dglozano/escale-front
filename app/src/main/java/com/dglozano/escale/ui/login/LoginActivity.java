package com.dglozano.escale.ui.login;

import android.app.ActivityOptions;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.dglozano.escale.R;
import com.dglozano.escale.databinding.ActivityLoginBinding;
import com.dglozano.escale.ui.BaseActivity;
import com.dglozano.escale.ui.Event;
import com.dglozano.escale.ui.main.MainActivity;
import com.dglozano.escale.web.EscaleRestApi;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.AndroidInjection;

public class LoginActivity extends BaseActivity {

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
        // Enable transitions
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(LoginActivityViewModel.class);

        // Inflate view and obtain an instance of the binding class.
        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        // Assign the component to a property in the binding class.
        binding.setViewmodel(mViewModel);

        // Set an exit transition
        Slide slide = new Slide();
        slide.setSlideEdge(Gravity.LEFT);
        getWindow().setEnterTransition(slide);
        getWindow().setExitTransition(slide);

        ButterKnife.bind(this);

        mViewModel.getLoggedUserId().observe(this, this::onLoggedUserChange);
        mViewModel.getLoading().observe(this, this::onLoadingStateChange);
        mViewModel.getErrorEvent().observe(this, this::onErrorEventFired);
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        finish();
    }

    @OnClick(R.id.login_sign_in_button)
    public void signIn() {
        mViewModel.hitLogin(mEmailEditText.getText().toString(), mPasswordEditText.getText().toString());
    }

    @Override
    protected View getRootLayout() {
        return findViewById(android.R.id.content);
    }

    private void onLoggedUserChange(Long id) {
        if (id != null && id != -1) {
            startMainActivity();
        }
    }

    private void onLoadingStateChange(Boolean isLoading) {
        if (isLoading != null && isLoading) {
            mProgressBarContainer.setVisibility(View.VISIBLE);
        } else {
            mProgressBarContainer.setVisibility(View.GONE);
        }
    }

    private void onErrorEventFired(Event<Integer> errorEvent) {
        if (errorEvent != null && !errorEvent.hasBeenHandled()) {
            showSnackbarWithOkDismiss(errorEvent.getContentIfNoThandled());
        }
    }
}
