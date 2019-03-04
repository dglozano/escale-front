package com.dglozano.escale.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.dglozano.escale.db.entity.AppUser;
import com.dglozano.escale.db.entity.Chat;
import com.dglozano.escale.db.entity.UserChatJoin;

import java.util.List;

@Dao
public interface UserChatJoinDao {
    @Insert
    void insert(UserChatJoin userChatJoin);

//    @Query("SELECT * FROM appuser INNER JOIN user_chat_join " +
//            "ON appuser.id=user_chat_join.userId " +
//            "WHERE user_chat_join.chatId=:chatId")
//    List<AppUser> getUsersForChat(final Long chatId);

    @Query("SELECT * FROM chat INNER JOIN user_chat_join " +
            "ON chat.id=user_chat_join.chatId " +
            "WHERE user_chat_join.userId=:userId")
            LiveData<List<Chat>> getChatsForUserAsLiveData(final Long userId);

    @Query("SELECT id FROM chat INNER JOIN user_chat_join " +
            "ON chat.id=user_chat_join.chatId " +
            "WHERE user_chat_join.userId=:patientId " +
            "LIMIT 1")
    LiveData<Long> getChatOfLoggedPatient(final Long patientId);
}