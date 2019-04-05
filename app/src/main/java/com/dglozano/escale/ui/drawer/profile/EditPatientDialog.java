package com.dglozano.escale.ui.drawer.profile;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dglozano.escale.R;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import butterknife.Unbinder;
import timber.log.Timber;

public class EditPatientDialog extends DialogFragment {

    @BindView(R.id.dialog_edit_user_height_inputlayout)
    TextInputLayout mHeightInputLayout;
    @BindView(R.id.dialog_edit_user_height_edittext)
    EditText mHeightEditText;
    @BindView(R.id.dialog_edit_user_ph_activity_seekbar)
    SeekBar mPhActivitySeekBar;
    @BindView(R.id.dialog_edit_user_ph_activity_description)
    TextView mPhActivityDescription;
    @BindString(R.string.dialog_edit_user_ph_activity_description_1)
    String mDescription1;
    @BindString(R.string.dialog_edit_user_ph_activity_description_2)
    String mDescription2;
    @BindString(R.string.dialog_edit_user_ph_activity_description_3)
    String mDescription3;
    @BindString(R.string.dialog_edit_user_ph_activity_description_4)
    String mDescription4;
    @BindString(R.string.dialog_edit_user_ph_activity_description_5)
    String mDescription5;

    private Unbinder mViewUnbinder;
    private int mCurrentHeight;
    private int mCurrentActivity;

    public interface EditPatientDialogListener {
        void onEditSubmit();
    }

    private EditPatientDialogListener mListener;

    public static EditPatientDialog newInstance(int currentHeight, int currentPhysicalActivity) {
        EditPatientDialog f = new EditPatientDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("currentHeight", currentHeight);
        args.putInt("currentActivity", currentPhysicalActivity);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mListener = (EditPatientDialogListener) getActivity();

            if (getArguments() != null) {
                mCurrentHeight = getArguments().getInt("currentHeight", -1);
                mCurrentActivity = getArguments().getInt("currentActivity", -1);
            } else {
                mCurrentActivity = -1;
                mCurrentHeight = -1;
            }

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

        View dialogView = inflater.inflate(R.layout.dialog_edit_user_height_activity, null);
        mViewUnbinder = ButterKnife.bind(this, dialogView);

        mHeightEditText.setText(mCurrentHeight == -1 ? "" : mCurrentHeight + "");
        mPhActivitySeekBar.setProgress(mCurrentActivity == -1 ? 2 : mCurrentActivity - 1);
        mPhActivityDescription.setText(getDescriptionFromActivityNumber(mCurrentActivity));
        mPhActivitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPhActivityDescription.setText(getDescriptionFromActivityNumber(progress + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        builder.setView(dialogView)
                .setPositiveButton(R.string.dialog_edit_user_submit, (dialog, id) -> {
                })
                .setNegativeButton(android.R.string.cancel,
                        (dialog, which) -> Timber.d("Edit cancelled"));

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        final AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                if (isHeightInputValid()) {
                    mListener.onEditSubmit();
                    d.dismiss();
                }
            });
        }
    }

    private String getDescriptionFromActivityNumber(int phActivity) {
        switch (phActivity) {
            case 1:
                return mDescription1;
            case 2:
                return mDescription2;
            case 3:
                return mDescription3;
            case 4:
                return mDescription4;
            case 5:
                return mDescription5;
            default:
                return mDescription3;
        }
    }

    public int getInputHeight() {
        return Integer.parseInt(mHeightEditText.getText().toString());
    }

    public int getInputActivity() {
        return mPhActivitySeekBar.getProgress() + 1;
    }

    @OnTextChanged(value = R.id.dialog_edit_user_height_edittext,
            callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void afterEditTextChangedMuscle(Editable editable) {
        Integer errorString = getHeightInputErrorStringResource(editable);
        mHeightInputLayout.setError(errorString == null ? null : getString(errorString));
    }

    @Nullable
    private Integer getHeightInputErrorStringResource(Editable editable) {
        Integer errorString;
        if (!TextUtils.isEmpty(editable)) {
            float value = Integer.parseInt(editable.toString());
            errorString = value >= 100 && value <= 250 ? null : R.string.height_range_error;
        } else {
            errorString = R.string.add_measurement_empty_error;
        }
        return errorString;
    }

    private boolean isHeightInputValid() {
        return getHeightInputErrorStringResource(mHeightEditText.getText()) == null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.d("onDestroyView().");
        mViewUnbinder.unbind();
    }
}

