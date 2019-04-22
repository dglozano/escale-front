package com.dglozano.escale.web.dto;

public class DoctorDTO {

    private String firstName;
    private String lastName;
    private String email;
    private Long id;
    private boolean changedDefaultPassword;

    public DoctorDTO(String firstName, String lastName, String email, Long id, boolean changedDefaultPassword) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.id = id;
        this.changedDefaultPassword = changedDefaultPassword;
    }

    public DoctorDTO() {
    }

    public boolean hasChangedDefaultPassword() {
        return changedDefaultPassword;
    }

    public void setChangedDefaultPassword(boolean changedDefaultPassword) {
        this.changedDefaultPassword = changedDefaultPassword;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
