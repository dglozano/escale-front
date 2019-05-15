package com.dglozano.escale.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.dglozano.escale.R;
import com.dglozano.escale.ui.login.LoginActivity;
import com.dglozano.escale.util.NetworkUtil;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;

public class NoInternetActivity extends AppCompatActivity {

    @BindView(R.id.no_internet_progress_bar_container)
    RelativeLayout mProgressBarContainer;

    private Disposable mNetworkDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.no_internet_try_again_btn)
    public void tryAgain() {
        showProgressDialog();
        mNetworkDisposable = NetworkUtil.checkInternetAccess(this).subscribe(hasInternet -> {
            if (hasInternet) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                snackbarMessage();
            }
        }, (Throwable throwable) -> snackbarMessage());
    }

    private void snackbarMessage() {
        hideProgressDialog();
        Snackbar
                .make(getRootLayout(),
                        getResources().getString(R.string.snackbar_no_connection_detected),
                        Snackbar.LENGTH_SHORT)
                .show();
    }

    private void hideProgressDialog() {
        if (mProgressBarContainer.getVisibility() == View.VISIBLE) {
            mProgressBarContainer.setVisibility(View.GONE);
        }
    }

    private void showProgressDialog() {
        mProgressBarContainer.setVisibility(View.VISIBLE);
    }

    private View getRootLayout() {
        return findViewById(android.R.id.content);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mNetworkDisposable != null) mNetworkDisposable.dispose();
    }
}
