package com.dglozano.escale.web.dto;

import java.util.Date;

public class SendChatMessageDTO {
    private Date sentDate;
    private String message;

    public SendChatMessageDTO() {
    }

    public SendChatMessageDTO(String message, Date sentDate) {
        this.sentDate = sentDate;
        this.message = message;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
