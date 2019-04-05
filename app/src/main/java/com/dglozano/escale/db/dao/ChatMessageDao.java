package com.dglozano.escale.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dglozano.escale.db.entity.ChatMessage;

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
