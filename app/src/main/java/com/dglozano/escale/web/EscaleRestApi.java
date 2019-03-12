package com.dglozano.escale.web;

import com.dglozano.escale.web.dto.AddBodyMeasurementDTO;
import com.dglozano.escale.web.dto.BodyMeasurementDTO;
import com.dglozano.escale.web.dto.ChangePasswordDataDTO;
import com.dglozano.escale.web.dto.ChatDTO;
import com.dglozano.escale.web.dto.ChatMessageDTO;
import com.dglozano.escale.web.dto.Credentials;
import com.dglozano.escale.web.dto.DietDTO;
import com.dglozano.escale.web.dto.FirebaseTokenUpdateDTO;
import com.dglozano.escale.web.dto.LoginResponse;
import com.dglozano.escale.web.dto.PatientDTO;
import com.dglozano.escale.web.dto.SendChatMessageDTO;

import java.util.Date;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface EscaleRestApi {

    @POST("auth/login")
    Single<Response<LoginResponse>> login(@Body Credentials credentials);

    @POST("auth/refresh")
    Call<LoginResponse> refreshToken(@Header("refreshToken") String refreshToken);

    @GET("patients/{id}")
    Single<PatientDTO> getPatientById(@Path("id") Long patientId);

    @GET("patients/{id}/measurements")
    Call<List<BodyMeasurementDTO>> getAllBodyMeasurement(@Path("id") Long patientId);

    @GET("patients/{id}/last_measurements")
    Single<List<BodyMeasurementDTO>> getLastBodyMeasurements(@Path("id") Long patientId,
                                                     @Query("from") String isoFromDate,
                                                     @Query("limit") Integer limit);

    @POST("patients/{id}/measurements/add")
    Single<BodyMeasurementDTO> postNewMeasurement(@Body AddBodyMeasurementDTO addBodyMeasurementDTO,
                                              @Path("id") Long patientId);

    @GET("patients/{id}/diets")
    Single<List<DietDTO>> getDiets(@Path("id") Long patientId);

    @GET("chats")
    Single<List<ChatDTO>> getChatOfUser(@Query("user") Long userId);

    @POST("chats/create")
    Single<ChatDTO> createChatForLoggedUser(@Query("otherId") Long otherId);

    @GET("chats/{id}/messages")
    Single<List<ChatMessageDTO>> getChatMessages(@Path("id") Long chatId);

    @POST("chats/{id}/messages")
    Single<ChatMessageDTO> sendChatMessage(@Path("id") Long chatId, @Body SendChatMessageDTO chatMessageDTO);

    @Streaming
    @GET("diets/download/{uuid}")
    Call<ResponseBody> downloadDiet(@Path("uuid") String dietId);

    @POST("users/{id}/password_change")
    Single<Response<Void>> changePassword(@Body ChangePasswordDataDTO changePasswordData, @Path("id") Long userId);

    @POST("users/{id}/firebase_token")
    Call<Void> updateToken(@Path("id") Long userId, @Body FirebaseTokenUpdateDTO token);

    @POST("users/{id}/firebase_token")
    Completable deleteToken(@Path("id") Long userId);
}
