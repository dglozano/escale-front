package com.dglozano.escale.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.dglozano.escale.db.entity.AppUser;
import com.dglozano.escale.db.entity.Chat;
import com.dglozano.escale.db.entity.Diet;
import com.dglozano.escale.db.entity.UserChatJoin;

import java.util.List;
import java.util.Optional;

import io.reactivex.Completable;

@Dao
public interface ChatDao {
    @Insert
    Long insert(Chat chat);

    @Query("SELECT * FROM Chat WHERE id == :id")
    Optional<Chat> getChatById(Long id);

    @Query("SELECT COUNT(*) FROM Chat WHERE id == :id")
    Integer chatExists(Long id);
}