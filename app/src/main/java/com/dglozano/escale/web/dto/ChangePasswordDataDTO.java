package com.dglozano.escale.web.dto;

public class ChangePasswordDataDTO {

    private String currentPassword;
    private String newPassword;
    private String newPasswordRepeat;

    public ChangePasswordDataDTO() {
    }

    public ChangePasswordDataDTO(String currentPassword, String newPassword, String newPasswordRepeat) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.newPasswordRepeat = newPasswordRepeat;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPasswordRepeat() {
        return newPasswordRepeat;
    }

    public void setNewPasswordRepeat(String newPasswordRepeat) {
        this.newPasswordRepeat = newPasswordRepeat;
    }
}
