package com.dglozano.escale.db.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.annotation.NonNull;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "user_chat_join",
        primaryKeys = { "userId", "chatId" },
        foreignKeys = {
                @ForeignKey(entity = AppUser.class,
                        parentColumns = "id",
                        childColumns = "userId",
                        onDelete = CASCADE),
                @ForeignKey(entity = Chat.class,
                        parentColumns = "id",
                        childColumns = "chatId",
                        onDelete = CASCADE)
        })
public class UserChatJoin {
    @NonNull
    private final Long userId;
    @NonNull
    private final Long chatId;

    public UserChatJoin(final Long userId, final Long chatId) {
        this.userId = userId;
        this.chatId = chatId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getChatId() {
        return chatId;
    }
}
