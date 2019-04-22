package com.dglozano.escale.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "user_chat_msg_seen_join",
        primaryKeys = {"userId", "chatMessageId"},
        foreignKeys = {
                @ForeignKey(entity = AppUser.class,
                        parentColumns = "id",
                        childColumns = "userId",
                        onDelete = CASCADE),
                @ForeignKey(entity = ChatMessage.class,
                        parentColumns = "id",
                        childColumns = "chatMessageId",
                        onDelete = CASCADE)
        })
public class UserChatMsgSeenJoin {

    @NonNull
    private final Long userId;
    @NonNull
    private final Long chatMessageId;

    public UserChatMsgSeenJoin(@NonNull final Long userId, @NonNull final Long chatMessageId) {
        this.userId = userId;
        this.chatMessageId = chatMessageId;
    }

    @NonNull
    public Long getUserId() {
        return userId;
    }

    @NonNull
    public Long getChatMessageId() {
        return chatMessageId;
    }

}
