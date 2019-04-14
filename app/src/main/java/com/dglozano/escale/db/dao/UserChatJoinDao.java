package com.dglozano.escale.db.dao;

import com.dglozano.escale.db.entity.Chat;
import com.dglozano.escale.db.entity.UserChatJoin;

import java.util.List;
import java.util.Optional;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import io.reactivex.Maybe;
import io.reactivex.Single;

@Dao
public abstract class UserChatJoinDao extends BaseDao<UserChatJoin> {

    @Query("SELECT * FROM chat INNER JOIN user_chat_join " +
            "ON chat.id=user_chat_join.chatId " +
            "WHERE user_chat_join.userId=:userId")
    public abstract LiveData<List<Chat>> getChatsForUserAsLiveData(final Long userId);

    @Query("SELECT id FROM chat INNER JOIN user_chat_join " +
            "ON chat.id=user_chat_join.chatId " +
            "WHERE user_chat_join.userId=:patientId " +
            "LIMIT 1")
    public abstract LiveData<Long> getChatOfPatientAsLiveData(final Long patientId);

    @Query("SELECT id FROM chat INNER JOIN user_chat_join " +
            "ON chat.id=user_chat_join.chatId " +
            "WHERE user_chat_join.userId=:patientId " +
            "LIMIT 1")
    public abstract Single<Optional<Long>> getChatOfPatientAsOptional(final Long patientId);

    @Query("SELECT id FROM chat INNER JOIN user_chat_join " +
            "ON chat.id=user_chat_join.chatId " +
            "WHERE user_chat_join.userId=:patientId " +
            "LIMIT 1")
    public abstract Maybe<Long> getChatOfPatientAsMaybe(final Long patientId);
}