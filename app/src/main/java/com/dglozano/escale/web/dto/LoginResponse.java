package com.dglozano.escale.web.dto;

public class LoginResponse {

    private Long id;
    private String email;
    private int userType;
    private boolean enabled;

    public LoginResponse(Long id, String email, int userType, boolean enabled) {
        this.id = id;
        this.email = email;
        this.userType = userType;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return String.format("id: %s \n email: %s \n userType: %s \n isEnabled: %s",
                id, email, userType, enabled);
    }
}
