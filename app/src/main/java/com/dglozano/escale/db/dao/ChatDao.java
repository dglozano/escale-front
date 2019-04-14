package com.dglozano.escale.db.dao;

import com.dglozano.escale.db.entity.Chat;

import java.util.Optional;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public abstract class ChatDao extends BaseDao<Chat> {

    @Query("SELECT * FROM Chat WHERE id == :id")
    public abstract Optional<Chat> getChatById(Long id);

    @Query("SELECT COUNT(*) FROM Chat WHERE id == :id")
    public abstract Integer chatExists(Long id);
}