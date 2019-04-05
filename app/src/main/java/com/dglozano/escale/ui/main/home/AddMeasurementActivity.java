package com.dglozano.escale.ui.main.home;

import android.app.Activity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.dglozano.escale.R;
import com.dglozano.escale.databinding.ActivityAddManualMeasurementBinding;
import com.dglozano.escale.ui.BaseActivity;
import com.dglozano.escale.util.ui.Event;

import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import dagger.android.AndroidInjection;

public class AddMeasurementActivity extends BaseActivity {

    @BindView(R.id.add_measurement_progress_bar_container)
    RelativeLayout mProgressBarContainer;
    @BindView(R.id.add_measurement_weight_input_layout)
    TextInputLayout mWeightInputLayout;
    @BindView(R.id.add_measurement_water_input_layout)
    TextInputLayout mWaterInputLayout;
    @BindView(R.id.add_measurement_fat_input_layout)
    TextInputLayout mFatInputLayout;
    @BindView(R.id.add_measurement_imc_input_layout)
    TextInputLayout mBmiInputLayout;
    @BindView(R.id.add_measurement_muscles_input_layout)
    TextInputLayout mMusclesInputLayout;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private AddMeasurementViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(AddMeasurementViewModel.class);
        ActivityAddManualMeasurementBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_add_manual_measurement);
        binding.setViewmodel(mViewModel);
        ButterKnife.bind(this);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mViewModel.getLoading().observe(this, this::onLoadingStateChange);
        mViewModel.getErrorEvent().observe(this, this::onErrorEventFired);
        mViewModel.getSuccessEvent().observe(this, this::onSuccessEventFired);
    }

    private void onSuccessEventFired(Event<Boolean> successEvent) {
        setResult(Activity.RESULT_OK);
        finish();
    }

    private void onErrorEventFired(Event<Integer> errorEvent) {
        if (errorEvent != null && !errorEvent.hasBeenHandled()) {
            showSnackbarWithOkDismiss(errorEvent.handleContent());
        }
    }

    private void onLoadingStateChange(Boolean isLoading) {
        if (isLoading != null && isLoading) {
            mProgressBarContainer.setVisibility(View.VISIBLE);
        } else {
            mProgressBarContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }


    @OnClick(R.id.add_measurement_btn)
    public void onAddMeasurementBtnClick(View view) {
        mViewModel.hitAddMeasurement(
                Objects.requireNonNull(mWeightInputLayout.getEditText()).getText().toString(),
                Objects.requireNonNull(mWaterInputLayout.getEditText()).getText().toString(),
                Objects.requireNonNull(mFatInputLayout.getEditText()).getText().toString(),
                Objects.requireNonNull(mBmiInputLayout.getEditText()).getText().toString(),
                "0", // TODO: Think what to do with Bone Mass.
                Objects.requireNonNull(mMusclesInputLayout.getEditText()).getText().toString()
        );
    }

    @OnFocusChange(R.id.add_measurement_weight_input_edittext)
    public void onFocusChangeWeight(View v, boolean hasFocus) {
        if (!hasFocus) {
            Integer errorString = validateEditText(((EditText) v).getText(), false);
            mWeightInputLayout.setError(errorString == null ? null : getString(errorString));
        }
    }


    @OnFocusChange(R.id.add_measurement_water_input)
    public void onFocusChangeWater(View v, boolean hasFocus) {
        if (!hasFocus) {
            Integer errorString = validateEditText(((EditText) v).getText(), true);
            mWaterInputLayout.setError(errorString == null ? null : getString(errorString));
        }
    }


    @OnFocusChange(R.id.add_measurement_fat_input)
    public void onFocusChangeFat(View v, boolean hasFocus) {
        if (!hasFocus) {
            Integer errorString = validateEditText(((EditText) v).getText(), true);
            mFatInputLayout.setError(errorString == null ? null : getString(errorString));
        }
    }

    @OnFocusChange(R.id.add_measurement_imc_input)
    public void onFocusBmi(View v, boolean hasFocus) {
        if (!hasFocus) {
            Integer errorString = validateEditText(((EditText) v).getText(), false);
            mBmiInputLayout.setError(errorString == null ? null : getString(errorString));
        }
    }

    @OnFocusChange(R.id.add_measurement_muscles_input)
    public void onFocusChangeMuscle(View v, boolean hasFocus) {
        if (!hasFocus) {
            Integer errorString = validateEditText(((EditText) v).getText(), true);
            mMusclesInputLayout.setError(errorString == null ? null : getString(errorString));
        }
    }

    @OnTextChanged(value = R.id.add_measurement_muscles_input,
            callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void afterEditTextChangedMuscle(Editable editable) {
        Integer errorString = validateEditText(editable, true);
        mMusclesInputLayout.setError(errorString == null ? null : getString(errorString));
    }

    @OnTextChanged(value = R.id.add_measurement_weight_input_edittext,
            callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void afterEditTextChangedWeight(Editable editable) {
        Integer errorString = validateEditText(editable, false);
        mWeightInputLayout.setError(errorString == null ? null : getString(errorString));
    }

    @OnTextChanged(value = R.id.add_measurement_water_input,
            callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void afterEditTextChangedWater(Editable editable) {
        Integer errorString = validateEditText(editable, true);
        mWaterInputLayout.setError(errorString == null ? null : getString(errorString));
    }

    @OnTextChanged(value = R.id.add_measurement_fat_input,
            callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void afterEditTextChangedFat(Editable editable) {
        Integer errorString = validateEditText(editable, true);
        mFatInputLayout.setError(errorString == null ? null : getString(errorString));
    }


    @OnTextChanged(value = R.id.add_measurement_imc_input,
            callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void afterEditTextChangedImc(Editable editable) {
        Integer errorString = validateEditText(editable, true);
        mBmiInputLayout.setError(errorString == null ? null : getString(errorString));
    }

    private Integer validateEditText(Editable inputLayout, boolean isPercentage) {
        if (!TextUtils.isEmpty(inputLayout)) {
            float value = Float.parseFloat(inputLayout.toString());
            if (isPercentage) {
                return value >= 0f && value <= 100f ? null : R.string.add_measurement_percentage_range_error;
            } else {
                return value >= 0f ? null : R.string.add_measurement_percentage_range_error;
            }
        } else {
            return R.string.add_measurement_empty_error;
        }
    }

    @Override
    protected View getRootLayout() {
        return findViewById(android.R.id.content);
    }
}
