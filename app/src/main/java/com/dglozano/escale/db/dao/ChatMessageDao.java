package com.dglozano.escale.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.dglozano.escale.db.entity.ChatMessage;
import com.dglozano.escale.db.entity.Diet;

import java.util.List;
import java.util.Optional;

@Dao
public interface ChatMessageDao {

    @Query("SELECT * FROM ChatMessage WHERE chatId == :chatId ORDER BY sentDate DESC")
    LiveData<List<ChatMessage>> getAllMessagesOfChatAsLiveData(Long chatId);

    @Query("SELECT * FROM ChatMessage WHERE chatId == :chatId ORDER BY sentDate DESC")
    List<ChatMessage> getAllMessagesOfChat(Long chatId);

    @Query("SELECT * FROM ChatMessage WHERE id == :id")
    Optional<ChatMessage> getChatMessageById(Long id);

    //FIXME: Borrar REPLACE cuando entre en produccion
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChatMessage(ChatMessage chatMessage);

    @Query("SELECT COUNT(*) FROM ChatMessage WHERE id == :id")
    Integer chatMessageExists(Long id);

    @Delete
    void deleteDiet(ChatMessage chatMessage);
}
