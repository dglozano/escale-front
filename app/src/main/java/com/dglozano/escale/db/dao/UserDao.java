package com.dglozano.escale.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dglozano.escale.db.entity.AppUser;

import java.util.List;
import java.util.Optional;

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
