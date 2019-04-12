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

    public static boolean isValidHeight(CharSequence height) {
        int value = Integer.parseInt(height.toString());
        return !TextUtils.isEmpty(height) && value >= 100 && value <= 250;
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

    public static Integer getHeightError(CharSequence height) {
        if (TextUtils.isEmpty(height))
            return R.string.input_validation_empty_error;
        else
            return isValidHeight(height) ? null : R.string.input_validation_height_range_error;
    }
}
