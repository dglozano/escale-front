package com.dglozano.escale.db;

import android.arch.persistence.room.TypeConverter;

import com.dglozano.escale.db.entity.Patient;

import java.util.Date;

public class DatabaseConverters {
    @TypeConverter
    public Long dateToLong(Date date) {
        return date.getTime();
    }

    @TypeConverter
    public Date longToDate(Long time) {
        return new Date(time);
    }

    @TypeConverter
    public Integer genderToInt(Patient.Gender gender) {
        return gender.equals(Patient.Gender.MALE) ? 0 : 1;
    }

    @TypeConverter
    public Patient.Gender intToGender(Integer integer) {
        return integer == 0 ? Patient.Gender.MALE : Patient.Gender.FEMALE;
    }
}
