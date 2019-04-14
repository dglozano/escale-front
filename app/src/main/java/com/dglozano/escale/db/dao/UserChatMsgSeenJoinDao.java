package com.dglozano.escale.db.dao;

import com.dglozano.escale.db.entity.UserChatMsgSeenJoin;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

@Dao
public abstract class UserChatMsgSeenJoinDao extends BaseDao<UserChatMsgSeenJoin> {

    @Query("SELECT COUNT(*) FROM chatmessage AS cm, chat INNER JOIN user_chat_msg_seen_join " +
            "ON cm.id=user_chat_msg_seen_join.chatMessageId " +
            "WHERE chat.id = cm.chatId " +
            "AND chat.id = :chatId " +
            "AND :userId NOT IN (SELECT user_chat_msg_seen_join.userId FROM user_chat_msg_seen_join, chat, chatmessage " +
            "WHERE user_chat_msg_seen_join.chatMessageId = chatmessage.id " +
            "AND cm.id = chatmessage.id " +
            "AND chatmessage.chatId = :chatId)")
    public abstract LiveData<Integer> getAmountOfUnseenChatMessagesForUserInChat(final Long userId, final Long chatId);
}