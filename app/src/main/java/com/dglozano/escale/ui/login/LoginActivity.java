package com.dglozano.escale.ui.login;

import android.app.ActivityOptions;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

import com.dglozano.escale.R;
import com.dglozano.escale.databinding.ActivityLoginBinding;
import com.dglozano.escale.ui.common.BaseActivity;
import com.dglozano.escale.ui.main.MainActivity;
import com.dglozano.escale.web.EscaleRestApi;
import com.dglozano.escale.web.dto.LoginResponse;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.AndroidInjection;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.login_root)
    View mRootView;
    @BindView(R.id.login_progress_bar_container)
    RelativeLayout mProgressBarContainer;

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

        ButterKnife.bind(this);

        hideProgressDialog();

        // Check if there is a user logged in the app.
        int loggedUserId = sharedPreferences.getInt("loggedUserId", -1);
        if (loggedUserId != -1) {
            startMainActivity(loggedUserId);
        }
        // If the loggedUserId in SharedPreferences is -1, then no user is logged in. Proceed.
    }

    private void startMainActivity(int loggedUserId) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("user_id", loggedUserId);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        finish();
    }

    @OnClick(R.id.login_sign_in_button)
    public void signIn() {
        showProgressDialog();
        Call<LoginResponse> loginResponseCall = escaleRestApi.login(mViewModel.getCredentials());
        loginResponseCall.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.code() == 200 && response.body() != null) {
                    if(response.body().getUserType() != 2) {
                        // If the user had proper Credentials, but it is not a Patient,
                        // he can't use the mobile app.
                        hideProgressDialog();
                        showSnackbarWithOkDismiss(R.string.login_error_not_patient);
                        Timber.d("Login error - User is not a Patient");
                        return;
                    }
                    int loggedUserId = response.body().getId();
                    String newToken = response.headers().get("token");
                    String newRefreshToken = response.headers().get("refreshToken");
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("token", newToken);
                    editor.putString("refreshToken", newRefreshToken);
                    editor.putInt("loggedUserId", loggedUserId);
                    editor.apply();
                    startMainActivity(loggedUserId);
                } else {
                    hideProgressDialog();
                    Timber.d("Login error - Response code is not 200 (Bad Credentials)");
                    showSnackbarWithOkDismiss(R.string.login_error_bad_credentials);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                hideProgressDialog();
                Timber.d(t, "Login error - onFailure callback. No internet connection.");
                showSnackbarWithOkDismiss(R.string.login_error_could_not_reach_server);
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
