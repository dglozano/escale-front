package com.dglozano.escale.web;

import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.web.dto.BodyMeasurementDTO;
import com.dglozano.escale.web.dto.Credentials;
import com.dglozano.escale.web.dto.LoginResponse;
import com.dglozano.escale.web.dto.PatientDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface EscaleRestApi {

    @POST("auth/login")
    Call<LoginResponse> login(@Body Credentials credentials);

    @POST("auth/refresh")
    Call<LoginResponse> refreshToken(@Header("refreshToken") String refreshToken);

    @GET("patients/{id}")
    Call<PatientDTO> getPatientById(@Path("id") int patientId);

    @GET("patients/{id}/measurements")
    Call<List<BodyMeasurementDTO>> getAllBodyMeasurement(@Path("id") int patientId);

    @GET("patients/{id}/last-measurement")
    Call<BodyMeasurementDTO> getLastBodyMeasurement(@Path("id") int patientId);

}
