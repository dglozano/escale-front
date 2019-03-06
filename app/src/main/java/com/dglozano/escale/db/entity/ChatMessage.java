package com.dglozano.escale.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.dglozano.escale.web.dto.ChatMessageDTO;

import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(foreignKeys = {
        @ForeignKey(entity = AppUser.class,
                parentColumns = "id",
                childColumns = "userId"),
        @ForeignKey(entity = Chat.class,
                parentColumns = "id",
                childColumns = "chatId",
                onDelete = CASCADE)
})
public class ChatMessage {

    @PrimaryKey
    private Long id;
    private Long chatId;
    private Long userId;
    private String message;
    private Date sentDate;

    public ChatMessage() {

    }

    @Ignore
    public ChatMessage(Long id, Long chatId, Long userId, String message, Date sentDate) {
        this.id = id;
        this.chatId = chatId;
        this.userId = userId;
        this.message = message;
        this.sentDate = sentDate;
    }

    @Ignore
    public ChatMessage(ChatMessageDTO dto, Long chatId) {
        this(dto.getId(), chatId, dto.getSenderId(), dto.getMessage(), dto.getSentDate());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }
}