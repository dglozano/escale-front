package com.dglozano.escale.web.dto;

import java.util.List;

public class ChatDTO {

    private Long id;
    private int messagesAmount;
    private List<Long> participantsIds;

    public ChatDTO() {
    }

    public ChatDTO(Long id, int messagesAmount, List<Long> participantsIds) {
        this.id = id;
        this.messagesAmount = messagesAmount;
        this.participantsIds = participantsIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getMessagesAmount() {
        return messagesAmount;
    }

    public void setMessagesAmount(int messagesAmount) {
        this.messagesAmount = messagesAmount;
    }

    public List<Long> getParticipantsIds() {
        return participantsIds;
    }

    public void setParticipantsIds(List<Long> participantsIds) {
        this.participantsIds = participantsIds;
    }
}
