package com.dglozano.escale.web;

import com.dglozano.escale.web.dto.BodyMeasurementDTO;
import com.dglozano.escale.web.dto.ChangePasswordDataDTO;
import com.dglozano.escale.web.dto.Credentials;
import com.dglozano.escale.web.dto.LoginResponse;
import com.dglozano.escale.web.dto.PatientDTO;

import java.util.List;

import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface EscaleRestApi {

    @POST("auth/login")
    Single<Response<LoginResponse>> login(@Body Credentials credentials);

    @POST("auth/refresh")
    Call<LoginResponse> refreshToken(@Header("refreshToken") String refreshToken);

    @GET("patients/{id}")
    Single<PatientDTO> getPatientById(@Path("id") Long patientId);

    @GET("patients/{id}/measurements")
    Call<List<BodyMeasurementDTO>> getAllBodyMeasurement(@Path("id") int patientId);

    @GET("patients/{id}/last-measurement")
    Call<BodyMeasurementDTO> getLastBodyMeasurement(@Path("id") int patientId);

    @POST("users/{id}/password_change")
    Single<Response<Void>> changePassword(@Body ChangePasswordDataDTO changePasswordData, @Path("id") Long userId);
}
