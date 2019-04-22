package com.dglozano.escale.db.dao;

import com.dglozano.escale.db.entity.Diet;

import java.util.List;
import java.util.Optional;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import io.reactivex.Single;

@Dao
public abstract class DietDao extends BaseDao<Diet> {

    @Query("SELECT * FROM Diet WHERE userId == :userId ORDER BY startDate DESC")
    public abstract LiveData<List<Diet>> getAllDietsOfUserWithIdAsLiveData(Long userId);

    @Query("SELECT * FROM Diet WHERE userId == :userId ORDER BY startDate DESC")
    public abstract List<Diet> getAllDietsOfUserWithId(Long userId);

    @Query("SELECT * FROM Diet")
    public abstract Single<List<Diet>> getAllDiets();

    @Query("SELECT * FROM Diet WHERE userId == :userId ORDER BY startDate DESC")
    public abstract Single<List<Diet>> getAllDietsOfUserWithIdAsSingle(Long userId);

    @Query("SELECT * FROM Diet WHERE userId == :userId ORDER BY startDate DESC LIMIT 1")
    public abstract LiveData<Diet> getCurrenDietOfUserWithIdAsLiveData(Long userId);

    @Query("SELECT * FROM Diet WHERE userId == :userId ORDER BY startDate DESC LIMIT 1")
    public abstract LiveData<Optional<Diet>> getCurrenDietOfUserWithIdLiveOptional(Long userId);

    @Query("SELECT * FROM Diet WHERE id == :id")
    public abstract Optional<Diet> getDietById(String id);

    @Query("SELECT EXISTS(SELECT 1 FROM Diet WHERE id == :id LIMIT 1)")
    public abstract Integer dietExists(String id);

    @Query("SELECT COUNT(*) FROM Diet WHERE userId == :userId")
    public abstract LiveData<Integer> dietsCountOfPatient(Long userId);

    @Query("DELETE FROM Diet WHERE id == :uuid")
    public abstract void deleteDietById(String uuid);
}
