package com.example.dglozano.escale;

import android.app.Application;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.example.dglozano.escale.data.EscaleDatabase;
import com.example.dglozano.escale.data.dao.BodyMeasurementDao;
import com.example.dglozano.escale.data.dao.UserDao;
import com.example.dglozano.escale.data.entities.BodyMeasurement;
import com.example.dglozano.escale.data.entities.User;

public class EscaleApp extends Application {

    private EscaleDatabase escaleDatabase;

    @Override
    public void onCreate() {
        escaleDatabase = Room.databaseBuilder(this,
                EscaleDatabase.class,
                "EscaleDatabase")
                .fallbackToDestructiveMigration()
                .addCallback(sRoomDatabaseCallback)
                .build();
        super.onCreate();
    }

    public EscaleDatabase getEscaleDatabase() {
        return escaleDatabase;
    }

    private RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback(){

                @Override
                public void onOpen (@NonNull SupportSQLiteDatabase db){
                    super.onOpen(db);
                    new PopulateDbAsync(escaleDatabase).execute();
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
