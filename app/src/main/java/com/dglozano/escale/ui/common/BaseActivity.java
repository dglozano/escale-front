package com.dglozano.escale.ui.common;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.dglozano.escale.R;

public abstract class BaseActivity extends AppCompatActivity {

    protected abstract View getRootLayout();

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
}
