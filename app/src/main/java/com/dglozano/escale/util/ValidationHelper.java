package com.dglozano.escale.util;

import android.text.TextUtils;

import com.dglozano.escale.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

public class ValidationHelper {

    public static boolean isValidEmail(CharSequence email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidName(CharSequence name) {
        return !TextUtils.isEmpty(name) && name.toString().matches("^[\\p{L} .'-]+$");
    }

    /**
     * ^                 # start-of-string
     * (?=.*[0-9])       # a digit must occur at least once
     * (?=.*[a-zA-Z)     # a letter must occur at least once
     * (?=\S+$)          # no whitespace allowed in the entire string
     * .{8,100}          # anything, at least eight places and max 100
     * $                 # end-of-string
     */
    public static boolean isValidPassword(CharSequence password) {
        return !TextUtils.isEmpty(password) && password.toString().matches("^(?=.*[0-9])(?=.*[a-zA-Z])(?=\\S+$).{8,100}$");
    }

    public static boolean isValidFileName(CharSequence filename) {
        return !TextUtils.isEmpty(filename) && filename.toString().matches("^(?!\\.)(?!.*\\.$)(?!.*?\\.\\.)^[\\w\\-_,(). ]+$");
    }

    public static boolean isValidHeight(CharSequence height) {
        return isValidRange(height, 100, 250);
    }

    public static boolean isValidWeight(CharSequence weight) {
        return isValidRange(weight, 40, 250);

    }

    public static boolean isValidBmi(CharSequence bmi) {
        return isValidRange(bmi, 10, 60);
    }

    public static boolean isValidPercentage(CharSequence percentage) {
        return isValidRange(percentage, 0, 100);
    }

    private static boolean isValidRange(CharSequence valueString, int min, int max) {
        if (TextUtils.isEmpty(valueString)) {
            return false;
        } else {
            float value = Float.parseFloat(valueString.toString());
            return value > min && value < max;
        }
    }

    public static boolean isValidBirthday(CharSequence birthday) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            if (!TextUtils.isEmpty(birthday)) {
                Date date = sdf.parse(birthday.toString());
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.YEAR, -1);
                Date oneYearBefore = calendar.getTime();
                return date.before(oneYearBefore);
            } else {
                return false;
            }
        } catch (ParseException e) {
            Timber.e("Birthday parse error");
            return false;
        }
    }

    public static boolean isValidGoalDueDate(CharSequence goalDueDateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            if (!TextUtils.isEmpty(goalDueDateStr)) {
                Date goalDueDate = sdf.parse(goalDueDateStr.toString());
                Date today = Calendar.getInstance().getTime();
                return goalDueDate.after(today);
            } else {
                return false;
            }
        } catch (ParseException e) {
            Timber.e("Due date parse error");
            return false;
        }
    }

    public static boolean isValidGenre(int genre) {
        return genre == 1 || genre == 2;
    }

    public static boolean isValidPhysicalActivity(int phActivity) {
        return phActivity >= 1 && phActivity <= 5;
    }

    private static Integer getDefaultNameError(CharSequence input) {
        if (TextUtils.isEmpty(input))
            return R.string.input_validation_empty_error;
        else
            return isValidName(input) ? null : R.string.input_default_validation_error;
    }

    public static Integer getNameError(CharSequence name) {
        return getDefaultNameError(name);
    }

    public static Integer getLastNameError(CharSequence lastName) {
        return getDefaultNameError(lastName);
    }

    public static Integer getEmailError(CharSequence email) {
        if (TextUtils.isEmpty(email))
            return R.string.input_validation_empty_error;
        else
            return isValidEmail(email) ? null : R.string.input_validation_email_error;
    }

    public static Integer getBirthdayError(CharSequence birthday) {
        if (TextUtils.isEmpty(birthday))
            return R.string.input_validation_empty_error;
        else
            return isValidBirthday(birthday) ? null : R.string.input_validation_birthday_error;
    }

    public static Integer getGoalDueDateError(CharSequence goalDueDate) {
        if (TextUtils.isEmpty(goalDueDate))
            return R.string.input_validation_empty_error;
        else
            return isValidGoalDueDate(goalDueDate) ? null : R.string.input_validation_goal_due_date_error;
    }

    public static Integer getHeightError(CharSequence height) {
        if (TextUtils.isEmpty(height))
            return R.string.input_validation_empty_error;
        else
            return isValidHeight(height) ? null : R.string.input_validation_height_range_error;
    }

    public static Integer getWeightError(CharSequence weight) {
        if (TextUtils.isEmpty(weight))
            return R.string.input_validation_empty_error;
        else
            return isValidWeight(weight) ? null : R.string.input_validation_weight_error;
    }

    public static Integer getBmiError(CharSequence bmi) {
        if (TextUtils.isEmpty(bmi))
            return R.string.input_validation_empty_error;
        else
            return isValidBmi(bmi) ? null : R.string.input_validation_bmi_error;
    }

    public static Integer getPasswordError(CharSequence password) {
        if (TextUtils.isEmpty(password))
            return R.string.input_validation_empty_error;
        else if (isValidPassword(password)) {
            return null;
        } else if (!password.toString().matches(".*\\d.*")) {
            return R.string.password_validation_no_digits_error;
        } else if (!password.toString().matches(".*[a-zA-Z].*")) {
            return R.string.password_validation_no_letters_error;
        } else if (password.toString().length() < 8) {
            return R.string.password_validation_not_long_enough_error;
        } else if (password.toString().length() > 100) {
            return R.string.password_validation_too_long_error;
        } else {
            return R.string.password_validation_not_valid_char_error;
        }
    }
}
