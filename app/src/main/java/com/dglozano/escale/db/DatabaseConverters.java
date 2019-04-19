package com.dglozano.escale.db;

import com.dglozano.escale.db.entity.Alert;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.util.MyFileUtils;

import java.util.Date;

import androidx.room.TypeConverter;

public class DatabaseConverters {
    @TypeConverter
    public Long dateToLong(Date date) {
        return date != null ? date.getTime() : null;
    }

    @TypeConverter
    public Date longToDate(Long time) {
        return time != null ? new Date(time) : null;
    }

    @TypeConverter
    public Integer genderToInt(Patient.Gender gender) {
        return gender == null || gender.equals(Patient.Gender.MALE) ? 0 : 1;
    }

    @TypeConverter
    public Integer alertTypeToInt(Alert.AlertType alertType) {
        return Alert.alertTypeToInt(alertType);
    }

    @TypeConverter
    public Alert.AlertType intToAlertType(Integer i) {
        return Alert.intToAlertType(i);
    }

    @TypeConverter
    public Patient.Gender intToGender(Integer integer) {
        return integer == 0 ? Patient.Gender.MALE : Patient.Gender.FEMALE;
    }

    @TypeConverter
    public Integer fileStatusToInt(MyFileUtils.FileStatus fileStatus) {
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
    public MyFileUtils.FileStatus intToFileStatus(Integer integer) {
        switch (integer) {
            case 1:
                return MyFileUtils.FileStatus.DOWNLOADING;
            case 2:
                return MyFileUtils.FileStatus.DOWNLOADED;
            case 3:
                return MyFileUtils.FileStatus.NOT_DOWNLOADED;
        }
        return MyFileUtils.FileStatus.NOT_DOWNLOADED;
    }
}
