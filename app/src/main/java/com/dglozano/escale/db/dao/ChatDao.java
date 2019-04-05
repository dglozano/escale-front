package com.dglozano.escale.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.dglozano.escale.db.entity.Chat;

import java.util.Optional;

@Dao
public interface ChatDao {
    @Insert
    Long insert(Chat chat);

    @Query("SELECT * FROM Chat WHERE id == :id")
    Optional<Chat> getChatById(Long id);

    @Query("SELECT COUNT(*) FROM Chat WHERE id == :id")
    Integer chatExists(Long id);
}