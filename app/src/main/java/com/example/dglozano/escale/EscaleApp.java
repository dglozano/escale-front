package com.example.dglozano.escale;

import android.app.Application;
import android.arch.persistence.room.Room;

import com.example.dglozano.escale.db.EscaleDatabase;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import timber.log.Timber;

public class EscaleApp extends Application {

    private EscaleDatabase escaleDatabase;

    @Override
    public void onCreate() {
        escaleDatabase = Room.databaseBuilder(this,
                EscaleDatabase.class,
                "EscaleDatabase")
                .fallbackToDestructiveMigration()
                .build();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree() {
                //Add the line number to the tag

                @Override
                protected @Nullable String createStackElementTag(@NotNull StackTraceElement element) {
                    return super.createStackElementTag(element) + ':' + element.getLineNumber();
                }
            });
        }

        super.onCreate();
    }

    public EscaleDatabase getEscaleDatabase() {
        return escaleDatabase;
    }
}


/*
    private RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback() {

                @Override
                public void onOpen(@NonNull SupportSQLiteDatabase db) {
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
*/
