package com.dglozano.escale.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.dglozano.escale.R;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import timber.log.Timber;

public abstract class BaseActivity extends AppCompatActivity {

    protected abstract View getRootLayout();

    public void showSnackbarWithOkDismiss(int stringResource) {
        showSnackbarWithOkDismiss(getResources().getString(stringResource));
    }

    public void showSnackbarWithDuration(int stringResource, int duration) {
        showSnackbarWithDuration(getResources().getString(stringResource), duration);
    }

    public void showSnackbarWithDuration(String text, int duration) {
        Snackbar snackbar = Snackbar.make(getRootLayout(), text, duration);
        setSnackbarStyle(snackbar);
        snackbar.show();
    }

    public void showSnackbarWithOkDismiss(String text) {
        Snackbar snackbar = Snackbar.make(getRootLayout(), text, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, v -> {
                    // By default, the snackbar will be dismissed on click.
                });
        setSnackbarStyle(snackbar);
        snackbar.show();
    }

    private void setSnackbarStyle(Snackbar snackbar) {
        View snackbarView = snackbar.getView();
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setMaxLines(5);  // show multiple line
        textView.setTextColor(ContextCompat.getColor(this, R.color.white));
        snackbarView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        int padding = Math.round(getResources().getDimension(R.dimen.activity_vertical_margin_very_small));
        snackbarView.getRootView().setPadding(padding, padding, padding, padding);
        snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.colorAccent));
    }



    @Override
    protected void onPause() {
        super.onPause();
        Timber.d("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Timber.d("onStop");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("onResume");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate");
    }

}