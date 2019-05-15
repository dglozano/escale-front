package com.dglozano.escale.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.dglozano.escale.R;
import com.dglozano.escale.util.ui.Event;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import timber.log.Timber;

public abstract class BaseActivity extends AppCompatActivity {

    protected abstract View getRootLayout();

    public void showSnackbarWithOkDismiss(int stringResource) {
        showSnackbarWithOkDismiss(getResources().getString(stringResource));
    }

    public void showErrorSnackbarWithOkDismiss(int stringResource) {
        showErrorSnackbarWithOkDismiss(getResources().getString(stringResource));
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

    public void showErrorSnackbarWithOkDismiss(String text) {
        Snackbar snackbar = Snackbar.make(getRootLayout(), text, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, v -> {
                    // By default, the snackbar will be dismissed on click.
                });
        setSnackbarErrorStyle(snackbar);
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

    private void setSnackbarErrorStyle(Snackbar snackbar) {
        View snackbarView = snackbar.getView();
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setMaxLines(5);  // show multiple line
        textView.setTextColor(ContextCompat.getColor(this, R.color.white));
        snackbarView.setBackgroundColor(ContextCompat.getColor(this, R.color.errorRed));
        int padding = Math.round(getResources().getDimension(R.dimen.activity_vertical_margin_very_small));
        snackbarView.getRootView().setPadding(padding, padding, padding, padding);
        snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.errorLightRed));
    }

    public void onErrorEventFired(Event<Integer> errorEvent) {
        if (errorEvent != null && errorEvent.peekContent() != null && !errorEvent.hasBeenHandled()) {
            showErrorSnackbarWithOkDismiss(errorEvent.handleContent());
        }
    }

    public void setErrorInInputLayout(Integer error, TextInputLayout inputLayout) {
        if (error != null) {
            inputLayout.setError(getString(error));
        } else {
            inputLayout.setError(null);
        }
    }

    public void onSuccessEventFired(Event<?> successEvent) {
        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
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