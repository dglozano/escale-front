package com.dglozano.escale.ui;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.dglozano.escale.R;
import com.dglozano.escale.ui.common.NoInternetActivity;
import com.dglozano.escale.util.NetworkChangeReceiver;
import com.dglozano.escale.util.NetworkUtil;

import io.reactivex.disposables.Disposable;

public abstract class BaseActivity extends AppCompatActivity {

    protected abstract View getRootLayout();

    private NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
    private Disposable mNetworkDisposable;

    protected void showSnackbarWithOkDismiss(String text) {
        Snackbar snackbar = Snackbar.make(getRootLayout(), text, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, v -> {
                    // By default, the snackbar will be dismissed on click.
                });
        View snackbarView = snackbar.getView();
        TextView textView = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(5);  // show multiple line
        snackbar.show();
    }

    protected void showSnackbarWithOkDismiss(int stringResource) {
        showSnackbarWithOkDismiss(getResources().getString(stringResource));
    }

    protected void showSnackbarWithDuration(String text, int duration) {
        Snackbar snackbar = Snackbar.make(getRootLayout(), text, duration);
        View snackbarView = snackbar.getView();
        TextView textView = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(5);
        snackbar.show();
    }

    protected void showSnackbarWithDuration(int stringResource, int duration) {
        showSnackbarWithDuration(getResources().getString(stringResource), duration);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mNetworkDisposable != null ) mNetworkDisposable.dispose();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNetworkDisposable = NetworkUtil.checkInternetAccess(this).subscribe(hasInternet -> {
            if(!hasInternet) {
                showNoInternetActivity();
            }
        }, (Throwable throwable) -> showNoInternetActivity());
    }

    private void showNoInternetActivity() {
        Intent intent = new Intent(this, NoInternetActivity.class);
        startActivity(intent);
        finish();
    }
}
