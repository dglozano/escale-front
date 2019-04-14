package com.dglozano.escale.db.entity;

import com.dglozano.escale.web.dto.DoctorDTO;

import java.util.Date;

import androidx.room.Entity;
import androidx.room.Ignore;

@Entity
public class Doctor extends AppUser {


    public Doctor() {
    }

    @Ignore
    public Doctor(Long id, String firstName, String lastName, String email, Date lastUpdate) {
        super(id, firstName, lastName, email, lastUpdate);
    }

    @Ignore
    public Doctor(DoctorDTO doctorDTO, Date timestamp) {
        this(doctorDTO.getId(),
                doctorDTO.getFirstName(),
                doctorDTO.getLastName(),
                doctorDTO.getEmail(),
                timestamp);
    }
}
