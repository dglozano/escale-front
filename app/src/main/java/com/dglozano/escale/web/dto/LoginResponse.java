package com.dglozano.escale.web.dto;

public class LoginResponse {

    private int id;
    private String email;
    private int userType;

    public LoginResponse(int id, String email, int userType) {
        this.id = id;
        this.email = email;
        this.userType = userType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }
}
