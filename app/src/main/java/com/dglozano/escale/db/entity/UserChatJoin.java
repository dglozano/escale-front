package com.dglozano.escale.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "user_chat_join",
        primaryKeys = {"userId", "chatId"},
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

    public UserChatJoin(@NonNull final Long userId, @NonNull final Long chatId) {
        this.userId = userId;
        this.chatId = chatId;
    }

    @NonNull
    public Long getUserId() {
        return userId;
    }

    @NonNull
    public Long getChatId() {
        return chatId;
    }
}
