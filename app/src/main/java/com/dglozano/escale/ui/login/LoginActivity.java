package com.dglozano.escale.ui.login;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;

import com.dglozano.escale.R;
import com.dglozano.escale.databinding.ActivityLoginBinding;
import com.dglozano.escale.ui.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.AndroidInjection;
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
    FirebaseAuth mAuth;

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
    }


    @OnClick(R.id.login_sign_in_button)
    public void signIn() {
        showProgressDialog();
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
    protected void onResume() {
        super.onResume();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            startSignedInActivity(null);
            finish();
        }
    }

    private void startSignedInActivity(@Nullable FirebaseUser response) {
        startActivity(MainActivity.createIntent(this, response));
    }

    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

}
