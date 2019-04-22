package com.dglozano.escale.db.dao;

import com.dglozano.escale.db.entity.ChatMessage;

import java.util.List;
import java.util.Optional;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

@Dao
public abstract class ChatMessageDao extends BaseDao<ChatMessage> {

    @Query("SELECT * FROM ChatMessage WHERE chatId == :chatId ORDER BY sentDate DESC")
    public abstract LiveData<List<ChatMessage>> getAllMessagesOfChatAsLiveData(Long chatId);

    @Query("SELECT * FROM ChatMessage WHERE chatId == :chatId ORDER BY sentDate DESC")
    public abstract List<ChatMessage> getAllMessagesOfChat(Long chatId);

    @Query("SELECT * FROM ChatMessage WHERE id == :id")
    public abstract Optional<ChatMessage> getChatMessageById(Long id);

    @Query("SELECT COUNT(*) FROM ChatMessage WHERE id == :id")
    public abstract Integer chatMessageExists(Long id);
}
