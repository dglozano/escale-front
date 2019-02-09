package com.dglozano.escale.ui.common;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import com.dglozano.escale.R;
import com.dglozano.escale.databinding.ActivityChangePasswordBinding;
import com.dglozano.escale.databinding.ActivityLoginBinding;
import com.dglozano.escale.ui.login.LoginActivityViewModel;
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

public class ChangePasswordActivity extends BaseActivity {

    @BindView(R.id.change_password_progress_bar_container)
    RelativeLayout mProgressBarContainer;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    @Inject
    EscaleRestApi escaleRestApi;

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

        if(userId == -1) {
            finish();
        } else {
            mViewModel.setUserId(userId);
        }
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
