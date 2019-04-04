package com.dglozano.escale.web.dto;

public class FirebaseTokenUpdateDTO {

    private String token;

    public FirebaseTokenUpdateDTO() {
    }

    public FirebaseTokenUpdateDTO(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
