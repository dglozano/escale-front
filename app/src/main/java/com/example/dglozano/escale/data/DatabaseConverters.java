package com.example.dglozano.escale.data;

import android.arch.persistence.room.TypeConverter;

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
    public Integer genderToInt(User.Gender gender) {
        return gender.equals(User.Gender.MALE) ? 0 : 1;
    }

    @TypeConverter
    public User.Gender intToGender(Integer integer) {
        return integer == 0 ? User.Gender.MALE : User.Gender.FEMALE;
    }
}
