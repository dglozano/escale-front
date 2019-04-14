package com.dglozano.escale.web.dto;

import java.util.Date;
import java.util.List;

public class ChatMessageDTO {

    private Long id;
    private Long chatId;
    private Long senderId;
    private String message;
    private Date sentDate;
    private List<Long> seenBy;

    public ChatMessageDTO() {
    }

    public ChatMessageDTO(Long id, Long senderId, String message, Date sentDate, Long chatId, List<Long> seenBy) {
        this.id = id;
        this.senderId = senderId;
        this.message = message;
        this.sentDate = sentDate;
        this.chatId = chatId;
        this.seenBy = seenBy;
    }

    public List<Long> getSeenBy() {
        return seenBy;
    }

    public void setSeenBy(List<Long> seenBy) {
        this.seenBy = seenBy;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
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
