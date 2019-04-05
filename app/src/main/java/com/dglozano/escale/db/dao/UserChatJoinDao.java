package com.dglozano.escale.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.dglozano.escale.db.entity.Chat;
import com.dglozano.escale.db.entity.UserChatJoin;

import java.util.List;
import java.util.Optional;

import io.reactivex.Maybe;
import io.reactivex.Single;

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
    LiveData<Long> getChatOfLoggedPatientAsLiveData(final Long patientId);

    @Query("SELECT id FROM chat INNER JOIN user_chat_join " +
            "ON chat.id=user_chat_join.chatId " +
            "WHERE user_chat_join.userId=:patientId " +
            "LIMIT 1")
    Single<Optional<Long>> getChatOfLoggedPatientAsOptional(final Long patientId);

    @Query("SELECT id FROM chat INNER JOIN user_chat_join " +
            "ON chat.id=user_chat_join.chatId " +
            "WHERE user_chat_join.userId=:patientId " +
            "LIMIT 1")
    Maybe<Long> getChatOfLoggedPatientAsMaybe(final Long patientId);
}