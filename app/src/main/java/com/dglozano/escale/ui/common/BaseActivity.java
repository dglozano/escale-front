package com.dglozano.escale.ui.common;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.dglozano.escale.R;

public abstract class BaseActivity extends AppCompatActivity {

    protected abstract View getRootLayout();

    protected void showSnackbarWithOkDismiss(String text) {
        Snackbar.make(getRootLayout(), text, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, v -> {
                    // By default, the snackbar will be dismissed on click.
                })
                .show();
    }

    protected void showSnackbarWithOkDismiss(int stringResource) {
        showSnackbarWithOkDismiss(getResources().getString(stringResource));
    }

    protected void showSnackbarWithDuration(String text, int duration) {
        Snackbar.make(getRootLayout(), text, duration).show();
    }

    protected void showSnackbarWithDuration(int stringResource, int duration) {
        showSnackbarWithDuration(getResources().getString(stringResource), duration);
    }
}
