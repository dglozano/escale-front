package com.dglozano.escale.ui.doctor.main;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dglozano.escale.R;
import com.dglozano.escale.databinding.ActivityAddPatientBinding;
import com.dglozano.escale.ui.BaseActivity;
import com.dglozano.escale.util.ui.Event;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;
import dagger.android.AndroidInjection;

import static com.dglozano.escale.util.ValidationHelper.getBirthdayError;
import static com.dglozano.escale.util.ValidationHelper.getEmailError;
import static com.dglozano.escale.util.ValidationHelper.getHeightError;
import static com.dglozano.escale.util.ValidationHelper.getLastNameError;
import static com.dglozano.escale.util.ValidationHelper.getNameError;

public class AddPatientActivity extends BaseActivity {

    @BindView(R.id.add_patient_progress_bar_container)
    RelativeLayout mProgressBarContainer;

    @BindView(R.id.add_patient_first_name_inputlayout)
    TextInputLayout mFirstNameInputLayout;
    @BindView(R.id.add_patient_last_name_inputlayout)
    TextInputLayout mLastNameInputLayout;
    @BindView(R.id.add_patient_email_inputlayout)
    TextInputLayout mEmailInputLayout;
    @BindView(R.id.add_patient_height_inputlayout)
    TextInputLayout mHeightInputLayout;
    @BindView(R.id.add_patient_birthday_inputlayout)
    TextInputLayout mBirthdayInputLayout;
    @BindView(R.id.add_patient_genre_radiogroup)
    RadioRealButtonGroup mGenreRadioGroup;
    @BindView(R.id.add_patient_ph_activity_seekbar)
    SeekBar mPhActivitySeekBar;
    @BindView(R.id.add_patient_ph_activity_description)
    TextView mPhActivityDescription;
    @BindView(R.id.add_patient_birthday_edittext)
    EditText mBirthdayEditText;

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

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    DatePickerDialog.OnDateSetListener onDateSetListener;
    DatePickerDialog mDatePickerDialog = null;

    private AddPatientViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(AddPatientViewModel.class);
        ActivityAddPatientBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_add_patient);
        binding.setViewmodel(mViewModel);
        ButterKnife.bind(this);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mPhActivitySeekBar.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());

        setBirthdayInput();

        mViewModel.getLoading().observe(this, this::onLoadingStateChange);
        mViewModel.getErrorEvent().observe(this, this::onErrorEventFired);
        mViewModel.getSuccessEvent().observe(this, this::onSuccessEventFired);
    }

    private void setBirthdayInput() {
        mBirthdayEditText.setKeyListener(null);
        onDateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
            Calendar myCalendar = Calendar.getInstance();
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            mBirthdayEditText.setText(sdf.format(myCalendar.getTime()));
        };
    }

    @OnClick(R.id.add_patient_birthday_edittext)
    public void onBirthdayClick(View v) {
        if (!v.hasFocus()) {
            showDatePickerDialog();
        } else {
            findViewById(v.getNextFocusForwardId()).requestFocus();
        }
    }

    @OnFocusChange(R.id.add_patient_birthday_edittext)
    public void onBirthdayFocus(View v, boolean hasFocus) {
        if (hasFocus) {
            showDatePickerDialog();
        }
    }

    private void showDatePickerDialog() {
        Calendar myCalendar = Calendar.getInstance();
        myCalendar.set(1990, 1, 1);
        if (mDatePickerDialog == null) {
            mDatePickerDialog = new DatePickerDialog(this,
                    onDateSetListener,
                    myCalendar.get(Calendar.YEAR),
                    myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH));
            mDatePickerDialog.setOnDismissListener(dialogInterface -> mDatePickerDialog = null);
            mDatePickerDialog.show();
        }
    }


    @OnClick(R.id.add_patient_btn)
    public void onAddPatientClick(View view) {
        mViewModel.hitAddPatient(
                Objects.requireNonNull(mFirstNameInputLayout.getEditText()).getText(),
                Objects.requireNonNull(mLastNameInputLayout.getEditText()).getText(),
                Objects.requireNonNull(mEmailInputLayout.getEditText()).getText(),
                Objects.requireNonNull(mBirthdayInputLayout.getEditText()).getText(),
                Objects.requireNonNull(mHeightInputLayout.getEditText()).getText(),
                mGenreRadioGroup.getPosition() + 1,
                mPhActivitySeekBar.getProgress() + 1
        );
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

    @OnFocusChange(R.id.add_patient_first_name_edittext)
    public void onFocusChangeFirstName(View v, boolean hasFocus) {
        if (!hasFocus) {
            setErrorInInputLayout(getNameError(((EditText) v).getText()), mFirstNameInputLayout);
        }
    }

    @OnFocusChange(R.id.add_patient_last_name_edittext)
    public void onFocusChangeLastName(View v, boolean hasFocus) {
        if (!hasFocus) {
            setErrorInInputLayout(getLastNameError(((EditText) v).getText()), mLastNameInputLayout);
        }
    }

    @OnFocusChange(R.id.add_patient_email_edittext)
    public void onFocusChangeEmail(View v, boolean hasFocus) {
        if (!hasFocus) {
            setErrorInInputLayout(getEmailError(((EditText) v).getText()), mEmailInputLayout);
        }
    }

    @OnFocusChange(R.id.add_patient_birthday_edittext)
    public void onFocusChangeBirthday(View v, boolean hasFocus) {
        if (!hasFocus) {
            setErrorInInputLayout(getBirthdayError(((EditText) v).getText()), mBirthdayInputLayout);
        }
    }

    @OnFocusChange(R.id.add_patient_height_edittext)
    public void onFocusChangeHeight(View v, boolean hasFocus) {
        if (!hasFocus) {
            setErrorInInputLayout(getHeightError(((EditText) v).getText()), mHeightInputLayout);
        }
    }

    private void setErrorInInputLayout(Integer error, TextInputLayout inputLayout) {
        if (error != null) {
            inputLayout.setError(getString(error));
        } else {
            inputLayout.setError(null);
        }
    }

    @OnTextChanged(value = R.id.add_patient_first_name_edittext,
            callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void afterEditTextFirstName(Editable editable) {
        Integer errorString = getNameError(editable);
        mFirstNameInputLayout.setError(errorString == null ? null : getString(errorString));
    }

    @OnTextChanged(value = R.id.add_patient_last_name_edittext,
            callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void afterEditTextLastName(Editable editable) {
        Integer errorString = getLastNameError(editable);
        mLastNameInputLayout.setError(errorString == null ? null : getString(errorString));
    }

    @OnTextChanged(value = R.id.add_patient_email_edittext,
            callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void afterEditTextEmail(Editable editable) {
        Integer errorString = getEmailError(editable);
        mEmailInputLayout.setError(errorString == null ? null : getString(errorString));
    }

    @OnTextChanged(value = R.id.add_patient_birthday_edittext,
            callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void afterEditTextBirthday(Editable editable) {
        Integer errorString = getBirthdayError(editable);
        mBirthdayInputLayout.setError(errorString == null ? null : getString(errorString));
    }

    @OnTextChanged(value = R.id.add_patient_height_edittext,
            callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void afterEditTextHeight(Editable editable) {
        Integer errorString = getHeightError(editable);
        mHeightInputLayout.setError(errorString == null ? null : getString(errorString));
    }

    @Override
    protected View getRootLayout() {
        return findViewById(android.R.id.content);
    }

    private class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mPhActivityDescription.setText(getDescriptionFromActivityNumber(progress + 1));
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

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }
}
