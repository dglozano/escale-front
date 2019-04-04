package com.dglozano.escale.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;

import com.dglozano.escale.web.dto.DoctorDTO;

import java.util.Date;

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
