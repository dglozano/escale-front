package com.dglozano.escale.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dglozano.escale.db.entity.Diet;

import java.util.List;
import java.util.Optional;

@Dao
public interface DietDao {

    @Query("SELECT * FROM Diet WHERE userId == :userId ORDER BY startDate DESC")
    LiveData<List<Diet>> getAllDietsOfUserWithIdAsLiveData(Long userId);

    @Query("SELECT * FROM Diet WHERE userId == :userId ORDER BY startDate DESC")
    List<Diet> getAllDietsOfUserWithId(Long userId);

    @Query("SELECT * FROM Diet WHERE userId == :userId ORDER BY startDate DESC LIMIT 1")
    LiveData<Diet> getCurrenDietOfUserWithIdAsLiveData(Long userId);

    @Query("SELECT * FROM Diet WHERE userId == :userId ORDER BY startDate DESC LIMIT 1")
    LiveData<Optional<Diet>> getCurrenDietOfUserWithIdLiveOptional(Long userId);

    @Query("SELECT * FROM Diet WHERE id == :id")
    Optional<Diet> getDietById(String id);

    @Update
    void updateDiet(Diet diet);

    //FIXME: Borrar REPLACE cuando entre en produccion
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDiet(Diet diet);

    @Query("SELECT COUNT(*) FROM Diet WHERE id == :id")
    Integer dietExists(String id);


    @Query("SELECT COUNT(*) FROM Diet WHERE userId == :userId")
    LiveData<Integer> dietsCountOfPatient(Long userId);

    @Delete
    void deleteDiet(Diet diet);

    @Query("DELETE FROM Diet WHERE id == :uuid")
    void deleteDietById(String uuid);
}
