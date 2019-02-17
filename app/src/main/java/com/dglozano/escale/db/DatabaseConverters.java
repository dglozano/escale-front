package com.dglozano.escale.db;

import android.arch.persistence.room.TypeConverter;

import com.dglozano.escale.db.entity.Diet;
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

    @TypeConverter
    public Integer fileStatusToInt(Diet.FileStatus fileStatus) {
        switch (fileStatus) {
            case DOWNLOADING:
                return 1;
            case DOWNLOADED:
                return 2;
            case NOT_DOWNLOADED:
                return 3;
        }
        return 2;
    }

    @TypeConverter
    public Diet.FileStatus intToFileStatus(Integer integer) {
        switch (integer) {
            case 1:
                return Diet.FileStatus.DOWNLOADING;
            case 2:
                return Diet.FileStatus.DOWNLOADED;
            case 3:
                return Diet.FileStatus.NOT_DOWNLOADED;
        }
        return Diet.FileStatus.NOT_DOWNLOADED;
    }
}
