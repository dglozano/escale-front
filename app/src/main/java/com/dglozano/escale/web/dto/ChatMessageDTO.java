package com.dglozano.escale.web.dto;

import java.util.Date;

public class ChatMessageDTO {

    private Long id;
    private Long senderId;
    private String message;
    private Date sentDate;

    public ChatMessageDTO() {
    }

    public ChatMessageDTO(Long id, Long senderId, String message, Date sentDate) {
        this.id = id;
        this.senderId = senderId;
        this.message = message;
        this.sentDate = sentDate;
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
