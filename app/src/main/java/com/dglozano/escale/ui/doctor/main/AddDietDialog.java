package com.dglozano.escale.ui.doctor.main;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.dglozano.escale.R;
import com.dglozano.escale.util.ValidationHelper;
import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import butterknife.Unbinder;
import timber.log.Timber;

public class AddDietDialog extends DialogFragment {

    @BindView(R.id.dialog_add_diet_filename_inputlayout)
    TextInputLayout mDietNameInputLayout;
    @BindView(R.id.dialog_add_diet_filename_edittext)
    EditText mDietNameEditText;

    private Unbinder mViewUnbinder;

    public interface AddDietDialogDialogListener {
        void onAddDietSubmit();
    }

    private AddDietDialogDialogListener mListener;
    private String mCurrentDietName;
    private Uri dietFileUri;

    public static AddDietDialog newInstance(String currentDietName, Uri dietFileUri) {
        AddDietDialog f = new AddDietDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("currentDietName", currentDietName);
        args.putParcelable("dietFileUri", dietFileUri);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mListener = (AddDietDialogDialogListener) getParentFragment();

            if (getArguments() != null) {
                mCurrentDietName = getArguments().getString("currentDietName");
                dietFileUri = getArguments().getParcelable("dietFileUri");
            } else {
                mCurrentDietName = "";
                dietFileUri = null;
            }

        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement AddDietDialogDialogListener interface");
        }
    }

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getParentFragment().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_add_diet, null);
        mViewUnbinder = ButterKnife.bind(this, dialogView);

        mDietNameEditText.setText(mCurrentDietName);

        builder.setView(dialogView)
                .setPositiveButton(R.string.dialog_add_diet_submit, (dialog, id) -> {
                })
                .setNegativeButton(android.R.string.cancel,
                        (dialog, which) -> Timber.d("Add diet cancelled"));

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        final AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                if (isDietNameInputValid()) {
                    mListener.onAddDietSubmit();
                    d.dismiss();
                }
            });
        }
    }

    public String getInputDietName() {
        return mDietNameEditText.getText().toString().trim();
    }

    public Uri getDietFileUri() {
        return dietFileUri;
    }

    @OnTextChanged(value = R.id.dialog_add_diet_filename_edittext,
            callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void afterEditTextChangedDietName(Editable editable) {
        Integer errorString = getHeightInputErrorStringResource(editable);
        mDietNameInputLayout.setError(errorString == null ? null : getString(errorString));
    }

    @Nullable
    private Integer getHeightInputErrorStringResource(Editable editable) {
        Integer errorString;
        if (editable.toString().length() > mDietNameInputLayout.getCounterMaxLength()) {
            errorString = R.string.diet_name_too_long;
        } else if (TextUtils.isEmpty(editable)) {
            errorString = R.string.input_validation_empty_error;
        } else {
            errorString = ValidationHelper.isValidFileName(editable) ? null : R.string.input_validation_diet_name;
        }
        return errorString;
    }

    private boolean isDietNameInputValid() {
        return getHeightInputErrorStringResource(mDietNameEditText.getText()) == null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.d("onDestroyView().");
        mViewUnbinder.unbind();
    }
}

