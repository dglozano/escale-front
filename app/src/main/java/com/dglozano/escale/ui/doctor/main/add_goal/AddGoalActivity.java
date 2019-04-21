package com.dglozano.escale.ui.doctor.main.add_goal;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.dglozano.escale.R;
import com.dglozano.escale.ui.BaseActivity;
import com.dglozano.escale.util.ui.Event;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;
import dagger.android.AndroidInjection;

import static com.dglozano.escale.util.ValidationHelper.getGoalDueDateError;
import static com.dglozano.escale.util.ValidationHelper.getWeightError;

public class AddGoalActivity extends BaseActivity {

    @BindView(R.id.add_goal_inputlayout)
    TextInputLayout mAddGoalInputLayout;
    @BindView(R.id.add_goal_edittext)
    EditText mAddGoalEditText;
    @BindView(R.id.add_goal_due_date_inputlayout)
    TextInputLayout mDueDateInputLayout;
    @BindView(R.id.add_goal_due_date_edittext)
    EditText mDueDateEditText;
    @BindView(R.id.add_goal_progress_bar_container)
    RelativeLayout mProgressBarContainer;
    @BindView(R.id.add_goal_direction_group)
    RadioRealButtonGroup mGoalDirectionGroup;

    DatePickerDialog.OnDateSetListener onDateSetListener;
    DatePickerDialog mDatePickerDialog = null;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private AddGoalActivityViewModel mViewModel;

    @Override
    protected View getRootLayout() {
        return findViewById(android.R.id.content);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goal);
        ButterKnife.bind(this);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(AddGoalActivityViewModel.class);
        ButterKnife.bind(this);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        setDueDateInput();

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

    private void setDueDateInput() {
        mDueDateEditText.setKeyListener(null);
        onDateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
            Calendar myCalendar = Calendar.getInstance();
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            mDueDateEditText.setText(sdf.format(myCalendar.getTime()));
        };
    }

    @OnClick(R.id.add_goal_btn)
    public void onAddGoalClick(View v) {
        mViewModel.hitChangeGoal(
                Objects.requireNonNull(mAddGoalEditText).getText(),
                Objects.requireNonNull(mDueDateEditText).getText(),
                mGoalDirectionGroup.getPosition() == 0
        );
    }

    @OnClick(R.id.add_goal_due_date_edittext)
    public void onDueDateClick(View v) {
        if (!v.hasFocus()) {
            showDatePickerDialog();
        } else {
            mDueDateEditText.clearFocus();
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @OnFocusChange(R.id.add_goal_due_date_edittext)
    public void onDueDateFocus(View v, boolean hasFocus) {
        if (hasFocus) {
            showDatePickerDialog();
        }
    }

    private void showDatePickerDialog() {
        Calendar myCalendar = Calendar.getInstance();
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

    @OnFocusChange(R.id.add_goal_edittext)
    public void onFocusChangeGoal(View v, boolean hasFocus) {
        if (!hasFocus) {
            Integer errorString = getWeightError(((EditText) v).getText().toString());
            mAddGoalInputLayout.setError(errorString == null ? null : getString(errorString));
        }
    }

    @OnTextChanged(value = R.id.add_goal_edittext,
            callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void afterEditTextChangedGoal(Editable editable) {
        Integer errorString = getWeightError(editable.toString());
        mAddGoalInputLayout.setError(errorString == null ? null : getString(errorString));
    }

    @OnFocusChange(R.id.add_goal_due_date_edittext)
    public void onFocusChangeBirthday(View v, boolean hasFocus) {
        if (!hasFocus) {
            Integer errorString = getGoalDueDateError(((EditText) v).getText());
            mDueDateInputLayout.setError(errorString == null ? null : getString(errorString));
        }
    }

    @OnTextChanged(value = R.id.add_goal_due_date_edittext,
            callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void afterEditTextBirthday(Editable editable) {
        Integer errorString = getGoalDueDateError(editable);
        mDueDateInputLayout.setError(errorString == null ? null : getString(errorString));
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(Activity.RESULT_CANCELED);
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}
