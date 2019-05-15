package com.dglozano.escale.ui.main.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.dglozano.escale.R;
import com.dglozano.escale.ble.CommunicationHelper;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

public class AskScaleCredentialsDialog extends DialogFragment {

    @BindView(R.id.dialog_credentials_index_spinner)
    Spinner mUserIndexSpinner;
    @BindView(R.id.dialog_credentials_pin_edittext)
    EditText mUserPinEditText;
    @BindView(R.id.dialog_credentials_title)
    TextView mTitle;
    @BindView(R.id.dialog_credentials_instructions)
    TextView mInstructions;

    private Unbinder mViewUnbinder;
    private boolean mIsDeleteDialog;
    private ScaleCredentialsDialogListener mListener;
    private Fragment parentFragment;

    public static AskScaleCredentialsDialog newInstance(boolean isDeleteDialog) {
        AskScaleCredentialsDialog f = new AskScaleCredentialsDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putBoolean("isDeleteDialog", isDeleteDialog);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            parentFragment = getParentFragment();
            mListener = (ScaleCredentialsDialogListener) parentFragment;

            if (getArguments() != null) {
                mIsDeleteDialog = getArguments().getBoolean("isDeleteDialog", false);
            } else {
                mIsDeleteDialog = false;
            }

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

        if (mIsDeleteDialog) {
            mTitle.setText(R.string.dialog_scale_full_title);
            mInstructions.setText(R.string.dialog_scale_full_instructions);
            builder.setView(dialogView)
                    .setPositiveButton(R.string.dialog_delete_user_btn, (dialog, id) ->
                            mListener.onScaleCredentialsDialogSubmitCredentials())
                    .setNegativeButton(android.R.string.cancel,
                            (dialog, which) -> mListener.onScaleCredentialsDialogCancel());
        } else {
            builder.setView(dialogView)
                    .setPositiveButton(android.R.string.ok, (dialog, id) ->
                            mListener.onScaleCredentialsDialogSubmitCredentials())
                    .setNegativeButton(R.string.dialog_credentials_create_new_user_btn,
                            (dialog, which) -> mListener.onScaleCredentialsDialogCreateNewUser());
        }

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
        Timber.d("Dialog PIN input text %s", PIN);
        PIN = CommunicationHelper.decToHex(Integer.parseInt(PIN));
        PIN = PIN.length() == 2 ? "00" + PIN : PIN;
        Timber.d("Dialog PIN after processing %s", PIN);

        index = "0" + index;
        PIN = CommunicationHelper.flipBytes(PIN);

        Timber.d("Dialog PIN final %s", PIN);

        return new CommunicationHelper.PinIndex(index, PIN);
    }

    public interface ScaleCredentialsDialogListener {
        void onScaleCredentialsDialogSubmitCredentials();

        void onScaleCredentialsDialogCreateNewUser();

        void onScaleCredentialsDialogCancel();
    }
}

