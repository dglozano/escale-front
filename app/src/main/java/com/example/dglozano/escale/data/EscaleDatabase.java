package com.example.dglozano.escale.data;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.example.dglozano.escale.data.dao.BodyMeasurementDao;
import com.example.dglozano.escale.data.dao.UserDao;
import com.example.dglozano.escale.data.entities.BodyMeasurement;
import com.example.dglozano.escale.data.entities.User;

@Database(entities = {BodyMeasurement.class, User.class}, version = 4, exportSchema = false)
@TypeConverters(DatabaseConverters.class)
public abstract class EscaleDatabase extends RoomDatabase {

    public abstract BodyMeasurementDao bodyMeasurementDao();
    public abstract UserDao userDao();

    private static final String DB_NAME = "EscaleDatabase";
    private static volatile EscaleDatabase INSTANCE;

    public static synchronized EscaleDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context,
                    EscaleDatabase.class,
                    DB_NAME)
                    .fallbackToDestructiveMigration()
                    .addCallback(sRoomDatabaseCallback)
                    .build();
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback(){

                @Override
                public void onOpen (@NonNull SupportSQLiteDatabase db){
                    super.onOpen(db);
                    new PopulateDbAsync(INSTANCE).execute();
                }
            };

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final UserDao mUserDao;
        private final BodyMeasurementDao mBodyMeasurementDao;
        private final EscaleDatabase db;


        private PopulateDbAsync(EscaleDatabase db) {
            this.db = db;
            mUserDao = db.userDao();
            mBodyMeasurementDao = db.bodyMeasurementDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            db.clearAllTables();

            int userId = mUserDao.insertUser(User.createMockUser()).intValue();

            mBodyMeasurementDao.insertBodyMeasurement(BodyMeasurement
                    .createMockBodyMeasurementForUser(userId));
            mBodyMeasurementDao.insertBodyMeasurement(BodyMeasurement
                    .createMockBodyMeasurementForUser(userId));

            return null;
        }
    }
}
