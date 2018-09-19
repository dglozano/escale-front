package com.example.dglozano.escale;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.example.dglozano.escale.data.BodyMeasurement;
import com.example.dglozano.escale.data.EscaleDatabase;
import com.example.dglozano.escale.data.User;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Month;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import static com.example.dglozano.escale.data.BodyMeasurement.createMockBodyMeasurementForUser;
import static com.example.dglozano.escale.data.User.createMockUser;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DatabaseTests {

    private static EscaleDatabase mDatabase;

    @BeforeClass
    public static void openDb() {
        Context mContext = InstrumentationRegistry.getTargetContext();
        mDatabase = Room.inMemoryDatabaseBuilder(
                mContext,
                EscaleDatabase.class)
                .build();
    }

    @AfterClass
    public static void closeDb() {
        mDatabase.close();
    }


    @Before
    public void prepareForEachTest() {
        mDatabase.clearAllTables();
    }

    @Test
    public void userInsertTest() {
        User userTest = createMockUser();

        Integer userId = mDatabase.userDao().insertUser(userTest).intValue();
        userTest.setId(userId);

        User returnedUser = mDatabase.userDao().getUserById(userId);

        assertThat(returnedUser, equalTo(userTest));
    }

    @Test
    public void bodyMeasurementInsertTest() {
        User userTest = createMockUser();
        Integer userId = mDatabase.userDao().insertUser(userTest).intValue();

        BodyMeasurement bodyMeasurementTest = createMockBodyMeasurementForUser(userId);
        Integer bodyMeasurementId = mDatabase.bodyMeasurementDao()
                .insertBodyMeasurement(bodyMeasurementTest).intValue();
        bodyMeasurementTest.setId(bodyMeasurementId);

        BodyMeasurement returnedBodyMeasurementTest = mDatabase.bodyMeasurementDao()
                .getBodyMeasurementById(bodyMeasurementId);

        assertThat(returnedBodyMeasurementTest, equalTo(bodyMeasurementTest));

        assertEquals(returnedBodyMeasurementTest, bodyMeasurementTest);
    }

    @Test
    public void getAllBodyMeasurements() {
        User userTest1 = createMockUser();
        User userTest2 = createMockUser();

        Integer userId1 = mDatabase.userDao().insertUser(userTest1).intValue();
        Integer userId2 = mDatabase.userDao().insertUser(userTest2).intValue();

        BodyMeasurement bodyMeasurementTest1 = createMockBodyMeasurementForUser(userId1);
        BodyMeasurement bodyMeasurementTest2 = createMockBodyMeasurementForUser(userId1);
        BodyMeasurement bodyMeasurementTest3 = createMockBodyMeasurementForUser(userId2);

        bodyMeasurementTest1.setId(mDatabase.bodyMeasurementDao()
                .insertBodyMeasurement(bodyMeasurementTest1).intValue());
        bodyMeasurementTest2.setId(mDatabase.bodyMeasurementDao()
                .insertBodyMeasurement(bodyMeasurementTest2).intValue());
        bodyMeasurementTest3.setId(mDatabase.bodyMeasurementDao()
                .insertBodyMeasurement(bodyMeasurementTest3).intValue());

        List<BodyMeasurement> bodyMeasurementTestList = mDatabase
                .bodyMeasurementDao()
                .getAllBodyMeasurement();

        assertEquals(bodyMeasurementTestList.size(), 3);
        assertThat(bodyMeasurementTestList, contains(
                bodyMeasurementTest1,
                bodyMeasurementTest2,
                bodyMeasurementTest3)
        );
    }

    @Test
    public void getAllBodyMeasurementsOfUser() {
        User userTest1 = createMockUser();
        User userTest2 = createMockUser();

        Integer userId1 = mDatabase.userDao().insertUser(userTest1).intValue();
        Integer userId2 = mDatabase.userDao().insertUser(userTest2).intValue();

        BodyMeasurement bodyMeasurementTest1 = createMockBodyMeasurementForUser(userId1);
        BodyMeasurement bodyMeasurementTest2 = createMockBodyMeasurementForUser(userId1);
        BodyMeasurement bodyMeasurementTest3 = createMockBodyMeasurementForUser(userId2);

        bodyMeasurementTest1.setId(mDatabase.bodyMeasurementDao()
                .insertBodyMeasurement(bodyMeasurementTest1).intValue());
        bodyMeasurementTest2.setId(mDatabase.bodyMeasurementDao()
                .insertBodyMeasurement(bodyMeasurementTest2).intValue());
        bodyMeasurementTest3.setId(mDatabase.bodyMeasurementDao()
                .insertBodyMeasurement(bodyMeasurementTest3).intValue());

        List<BodyMeasurement> bodyMeasurementTestList = mDatabase
                .bodyMeasurementDao()
                .getAllBodyMeasurementByUserId(userId1);

        assertEquals(bodyMeasurementTestList.size(), 2);
        assertThat(bodyMeasurementTestList, contains(
                bodyMeasurementTest1,
                bodyMeasurementTest2)
        );
        assertThat(bodyMeasurementTestList, not(contains(bodyMeasurementTest3)));
    }
}
