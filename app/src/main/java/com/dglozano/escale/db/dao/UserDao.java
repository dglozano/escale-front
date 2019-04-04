package com.dglozano.escale.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.dglozano.escale.db.entity.AppUser;
import com.dglozano.escale.db.entity.Patient;

import java.util.List;
import java.util.Optional;

import io.reactivex.Single;

@Dao
public interface UserDao {
    @Query("SELECT * FROM AppUser")
    LiveData<List<AppUser>> getAllUsersAsLiveData();

    @Query("SELECT * FROM AppUser WHERE id == :id")
    LiveData<AppUser> getUserByIdAsLiveData(Long id);

    @Query("SELECT * FROM AppUser WHERE id == :id")
    Optional<AppUser> getUserById(Long id);

    @Delete
    void deleteUser(AppUser user);

    //FIXME: Borrar REPLACE cuando entre en produccion
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long save(AppUser appUser);

    @Query("DELETE FROM AppUser")
    void deleteAll();
}
