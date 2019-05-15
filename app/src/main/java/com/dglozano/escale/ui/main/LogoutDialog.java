package com.dglozano.escale.ui.main;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.dglozano.escale.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import timber.log.Timber;

public class LogoutDialog extends DialogFragment {

    private LogoutDialogListener mListener;

    public static LogoutDialog newInstance() {
        LogoutDialog f = new LogoutDialog();

        Bundle args = new Bundle();
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mListener = (LogoutDialogListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement EditPatientDialogListener interface");
        }
    }

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_logout_confirm, null);

        builder.setView(dialogView)
                .setPositiveButton(R.string.logout_title, (dialog, id) -> {
                    mListener.onLogoutConfirmed();
                })
                .setNegativeButton(android.R.string.cancel,
                        (dialog, which) -> Timber.d("Logout cancelled"));

        return builder.create();
    }

    public interface LogoutDialogListener {
        void onLogoutConfirmed();
    }

}

