package com.dglozano.escale.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

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
    Optional<Diet> getCurrenDietOfUserWithId(Long userId);

    @Query("SELECT * FROM Diet WHERE id == :id")
    Optional<Diet> getDietById(String id);

    @Update
    void updateDiet(Diet diet);

    //FIXME: Borrar REPLACE cuando entre en produccion
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDiet(Diet diet);

    @Query("SELECT COUNT(*) FROM Diet WHERE id == :id")
    Integer dietExists(String id);

    @Delete
    void deleteDiet(Diet diet);
}
