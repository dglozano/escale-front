package com.dglozano.escale.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.dglozano.escale.db.dao.BodyMeasurementDao;
import com.dglozano.escale.db.dao.ChatDao;
import com.dglozano.escale.db.dao.ChatMessageDao;
import com.dglozano.escale.db.dao.DietDao;
import com.dglozano.escale.db.dao.DoctorDao;
import com.dglozano.escale.db.dao.ForecastDao;
import com.dglozano.escale.db.dao.PatientDao;
import com.dglozano.escale.db.dao.UserChatJoinDao;
import com.dglozano.escale.db.dao.UserDao;
import com.dglozano.escale.db.entity.AppUser;
import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.db.entity.Chat;
import com.dglozano.escale.db.entity.ChatMessage;
import com.dglozano.escale.db.entity.Diet;
import com.dglozano.escale.db.entity.Doctor;
import com.dglozano.escale.db.entity.MeasurementForecast;
import com.dglozano.escale.db.entity.MeasurementPrediction;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.db.entity.UserChatJoin;

@Database(entities = {BodyMeasurement.class,
        Patient.class,
        Diet.class,
        ChatMessage.class,
        Chat.class,
        UserChatJoin.class,
        AppUser.class,
        MeasurementForecast.class,
        MeasurementPrediction.class,
        Doctor.class,},
        version = 25,
        exportSchema = false)
@TypeConverters(DatabaseConverters.class)
public abstract class EscaleDatabase extends RoomDatabase {

    public abstract BodyMeasurementDao bodyMeasurementDao();

    public abstract PatientDao patientDao();

    public abstract DietDao dietDao();

    public abstract ChatMessageDao chatMessageDao();

    public abstract UserChatJoinDao userChatJoinDao();

    public abstract DoctorDao doctorDao();

    public abstract UserDao userDao();

    public abstract ChatDao chatDao();

    public abstract ForecastDao forecastDao();

}
