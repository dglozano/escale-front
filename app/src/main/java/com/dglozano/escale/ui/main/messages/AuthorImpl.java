package com.dglozano.escale.ui.main.messages;

import com.stfalcon.chatkit.commons.models.IUser;

public class AuthorImpl implements IUser {

    private Long id;
    private String name;

    public AuthorImpl(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return id.toString();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return null;
    }
}
