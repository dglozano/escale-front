package com.example.dglozano.escale;

import androidx.room.Room;
import android.content.Context;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.db.EscaleDatabase;
import com.dglozano.escale.db.entity.Patient;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.dglozano.escale.db.entity.BodyMeasurement.createMockBodyMeasurementForUser;
import static com.dglozano.escale.db.entity.Patient.createMockUser;
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
        Patient patientTest = createMockUser();

        Long userId = mDatabase.patientDao().save(patientTest).intValue();
        patientTest.setId(userId);

        Patient returnedPatient = mDatabase.patientDao().getPatientById(userId);

        assertThat(returnedPatient, equalTo(patientTest));
    }

    @Test
    public void bodyMeasurementInsertTest() {
        Patient patientTest = createMockUser();
        Long userId = mDatabase.patientDao().save(patientTest).intValue();

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
        Patient patientTest1 = createMockUser();
        Patient patientTest2 = createMockUser();

        Long userId1 = mDatabase.patientDao().save(patientTest1).intValue();
        Long userId2 = mDatabase.patientDao().save(patientTest2).intValue();

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
        Patient patientTest1 = createMockUser();
        Patient patientTest2 = createMockUser();

        Long userId1 = mDatabase.patientDao().save(patientTest1).intValue();
        Long userId2 = mDatabase.patientDao().save(patientTest2).intValue();

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
