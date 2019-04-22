package com.dglozano.escale.ui.main.messages;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

public class MessageImpl implements IMessage {

    private Long id;
    private String text;
    private AuthorImpl sender;
    private Date sentDate;

    public MessageImpl(Long id, String text, AuthorImpl sender, Date sentDate) {
        this.id = id;
        this.text = text;
        this.sender = sender;
        this.sentDate = sentDate;
    }

    @Override
    public String getId() {
        return id.toString();
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public IUser getUser() {
        return sender;
    }

    @Override
    public Date getCreatedAt() {
        return sentDate;
    }
}
