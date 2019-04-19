package com.dglozano.escale.db;

import com.dglozano.escale.db.dao.AlertDao;
import com.dglozano.escale.db.dao.BodyMeasurementDao;
import com.dglozano.escale.db.dao.ChatDao;
import com.dglozano.escale.db.dao.ChatMessageDao;
import com.dglozano.escale.db.dao.DietDao;
import com.dglozano.escale.db.dao.DoctorDao;
import com.dglozano.escale.db.dao.ForecastDao;
import com.dglozano.escale.db.dao.PatientDao;
import com.dglozano.escale.db.dao.PatientInfoDao;
import com.dglozano.escale.db.dao.UserChatJoinDao;
import com.dglozano.escale.db.dao.UserChatMsgSeenJoinDao;
import com.dglozano.escale.db.dao.UserDao;
import com.dglozano.escale.db.entity.Alert;
import com.dglozano.escale.db.entity.AppUser;
import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.db.entity.Chat;
import com.dglozano.escale.db.entity.ChatMessage;
import com.dglozano.escale.db.entity.Diet;
import com.dglozano.escale.db.entity.Doctor;
import com.dglozano.escale.db.entity.MeasurementForecast;
import com.dglozano.escale.db.entity.MeasurementPrediction;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.db.entity.PatientInfo;
import com.dglozano.escale.db.entity.UserChatJoin;
import com.dglozano.escale.db.entity.UserChatMsgSeenJoin;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {BodyMeasurement.class,
        Patient.class,
        Diet.class,
        ChatMessage.class,
        Chat.class,
        UserChatJoin.class,
        Alert.class,
        UserChatMsgSeenJoin.class,
        AppUser.class,
        MeasurementForecast.class,
        MeasurementPrediction.class,
        PatientInfo.class,
        Doctor.class,},
        version = 32,
        exportSchema = false)
@TypeConverters(DatabaseConverters.class)
public abstract class EscaleDatabase extends RoomDatabase {

    public abstract BodyMeasurementDao bodyMeasurementDao();

    public abstract PatientDao patientDao();

    public abstract DietDao dietDao();

    public abstract ChatMessageDao chatMessageDao();

    public abstract UserChatJoinDao userChatJoinDao();

    public abstract UserChatMsgSeenJoinDao userChatMsgSeenJoinDao();

    public abstract DoctorDao doctorDao();

    public abstract UserDao userDao();

    public abstract ChatDao chatDao();

    public abstract ForecastDao forecastDao();

    public abstract PatientInfoDao patientInfoDao();

    public abstract AlertDao alertDao();
}
