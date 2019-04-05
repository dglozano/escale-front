package com.dglozano.escale.db.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Chat {

    @PrimaryKey
    protected Long id;

    public Chat() {
    }

    @Ignore
    public Chat(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
