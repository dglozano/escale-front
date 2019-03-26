package com.dglozano.escale.ui.main.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.dglozano.escale.R;
import com.dglozano.escale.ble.CommunicationHelper;
import com.dglozano.escale.ui.main.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

public class AskScaleCredentialsDialog extends DialogFragment {

    @BindView(R.id.dialog_credentials_index_spinner)
    Spinner mUserIndexSpinner;
    @BindView(R.id.dialog_credentials_pin_edittext)
    EditText mUserPinEditText;

    private Unbinder mViewUnbinder;

    public interface ScaleCredentialsDialogListener {
        void onScaleCredentialsDialogLogin();
        void onScaleCredentialsDialogCreateNewUser();
        void onScaleCredentialsDialogCancel();
    }

    private ScaleCredentialsDialogListener mListener;
    private Fragment parentFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            parentFragment = getParentFragment();
            mListener = (ScaleCredentialsDialogListener) parentFragment;
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement ScaleCredentialsDialogListener interface");
        }
    }

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = parentFragment.getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_ask_scale_credentials, null);
        mViewUnbinder = ButterKnife.bind(this, dialogView);

        builder.setView(dialogView)
                .setPositiveButton(android.R.string.ok, (dialog, id) ->
                        mListener.onScaleCredentialsDialogLogin())
                .setNegativeButton(R.string.dialog_credentials_create_new_user_btn,
                        (dialog, which) -> mListener.onScaleCredentialsDialogCreateNewUser());
        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.d("onDestroyView().");
        mViewUnbinder.unbind();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        mListener.onScaleCredentialsDialogCancel();
    }

    public CommunicationHelper.PinIndex getEnteredPinIndex() {
        String index = mUserIndexSpinner.getSelectedItemPosition() + 1 + "";
        String PIN = mUserPinEditText.getText().toString();

        index = "0" + index;
        PIN = CommunicationHelper.flipBytes(CommunicationHelper.decToHex(Integer.parseInt(PIN)));

        return new CommunicationHelper.PinIndex(index, PIN);
    }
}

