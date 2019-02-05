package com.dglozano.escale.ui.login;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;

import com.dglozano.escale.R;
import com.dglozano.escale.databinding.ActivityLoginBinding;
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

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;

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
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(LoginActivityViewModel.class);

        // Inflate view and obtain an instance of the binding class.
        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        // Assign the component to a property in the binding class.
        binding.setViewmodel(mViewModel);

        ButterKnife.bind(this);

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
        startActivity(intent);
    }

    /**
     * 1 - Validate
     * 2 - Check what to do with the view model and edit texts
     * 3 - Show ProgressDialog
     * 4 - Call API with credentials
     * 5 - Check response.
     * 5.1 - If valid, save tokens in SharedPreferences and go to MainActivity.
     * 5.1.1 - If the user hasn't changed the default password, show dialog to change it.
     * 5.2 - If not, show error messages.
     */
    @OnClick(R.id.login_sign_in_button)
    public void signIn() {
        showProgressDialog();
        Call<LoginResponse> loginResponseCall = escaleRestApi.login(mViewModel.getCredentials());
        loginResponseCall.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.code() == 200) {
                    Timber.d(response.body().toString());
                    int userType = response.body().getUserType(); // TODO check user type
                    int loggedUserId = response.body().getId();
                    String newToken = response.headers().get("token");
                    String newRefreshToken = response.headers().get("refreshToken");
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("token", newToken);
                    editor.putString("refreshToken", newRefreshToken);
                    editor.putInt("loggedUserId", loggedUserId);
                    editor.apply();
                    hideProgressDialog();
                    startMainActivity(loggedUserId);
                } else {
                    Timber.d("Login error response code is not 200");
                    //TODO
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                hideProgressDialog();
                Timber.d(t, "Login error onFailure");
                //TODO
            }
        });

        // TODO
    }

    private void hideProgressDialog() {
        if (mProgressBarContainer.getVisibility() == View.VISIBLE) {
            mProgressBarContainer.setVisibility(View.GONE);
        }
    }

    private void showProgressDialog() {
        mProgressBarContainer.setVisibility(View.VISIBLE);
    }

}
