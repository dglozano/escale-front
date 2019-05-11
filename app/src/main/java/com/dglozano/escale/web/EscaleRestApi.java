package com.dglozano.escale.web;

import com.dglozano.escale.db.entity.Alert;
import com.dglozano.escale.db.entity.MeasurementForecast;
import com.dglozano.escale.db.entity.PatientInfo;
import com.dglozano.escale.web.dto.AddBodyMeasurementDTO;
import com.dglozano.escale.web.dto.AddWeightGoalDTO;
import com.dglozano.escale.web.dto.BodyMeasurementDTO;
import com.dglozano.escale.web.dto.ChangePasswordDataDTO;
import com.dglozano.escale.web.dto.ChatDTO;
import com.dglozano.escale.web.dto.ChatMessageDTO;
import com.dglozano.escale.web.dto.CreatePatientDTO;
import com.dglozano.escale.web.dto.Credentials;
import com.dglozano.escale.web.dto.DietDTO;
import com.dglozano.escale.web.dto.DoctorDTO;
import com.dglozano.escale.web.dto.FirebaseTokenUpdateDTO;
import com.dglozano.escale.web.dto.LoginResponse;
import com.dglozano.escale.web.dto.PasswordDTO;
import com.dglozano.escale.web.dto.PatientDTO;
import com.dglozano.escale.web.dto.SendChatMessageDTO;
import com.dglozano.escale.web.dto.UpdatePatientDTO;
import com.dglozano.escale.web.dto.WeightGoalDTO;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface EscaleRestApi {

    @POST("/api/auth/login")
    Single<Response<LoginResponse>> login(@Body Credentials credentials);

    @POST("/api/auth/refresh")
    Call<LoginResponse> refreshToken(@Header("refreshToken") String refreshToken);

    @GET("/api/patients/{id}")
    Single<PatientDTO> getPatientById(@Path("id") Long patientId);

    @PUT("/api/patients/{id}/update")
    Completable updatePatientWithId(@Body UpdatePatientDTO updatePatientDTO, @Path("id") Long patientId);

    @GET("/api/patients/{id}/last_measurements")
    Single<List<BodyMeasurementDTO>> getLastBodyMeasurements(@Path("id") Long patientId,
                                                             @Query("from") String isoFromDate,
                                                             @Query("limit") Integer limit);

    @GET("/api/patients/{id}/measurements/forecast")
    Single<Response<MeasurementForecast>> getMeasurementForecastOfUser(@Path("id") Long patientId,
                                                                       @Query("force") boolean force);

    @GET("/api/patients/{id}/measurements/forecast")
    Single<Response<MeasurementForecast>> getMeasurementForecastOfUser(@Path("id") Long patientId,
                                                                       @Query("predictionsNumber") int predictionsNumber,
                                                                       @Query("force") boolean force);


    @GET("/api/patients/{id}/measurements/forecast")
    Single<Response<MeasurementForecast>> getMeasurementForecastOfUser(@Path("id") Long patientId,
                                                                       @Query("alpha") double alpha,
                                                                       @Query("gamma") double gamma,
                                                                       @Query("predictionsNumber") int predictionsNumber,
                                                                       @Query("force") boolean force);

    @POST("/api/patients/{id}/measurements/add")
    Single<BodyMeasurementDTO> postNewMeasurement(@Body AddBodyMeasurementDTO addBodyMeasurementDTO,
                                                  @Path("id") Long patientId);

    @GET("/api/patients/{id}/diets")
    Single<List<DietDTO>> getDiets(@Path("id") Long patientId);

    @GET("/api/chats")
    Single<List<ChatDTO>> getChatOfUser(@Query("user") Long userId);

    @POST("/api/chats/create")
    Single<ChatDTO> createChatForLoggedUser(@Query("otherId") Long otherId);

    @GET("/api/chats/{id}/messages")
    Single<List<ChatMessageDTO>> getChatMessages(@Path("id") Long chatId);

    @POST("/api/chats/{id}/messages")
    Single<ChatMessageDTO> sendChatMessage(@Path("id") Long chatId, @Body SendChatMessageDTO chatMessageDTO);

    @POST("/api/chats/{id}/messages/seenBy")
    Completable markSeenByUser(@Path("id") Long chatId, @Query("userId") Long userId);

    @Streaming
    @GET("/api/diets/download/{uuid}")
    Call<ResponseBody> downloadDiet(@Path("uuid") String dietId);

    @POST("/api/users/{id}/password_change")
    Single<Response<Void>> changePassword(@Body ChangePasswordDataDTO changePasswordData, @Path("id") Long userId);

    @POST("/api/users/password_recovery")
    Single<Response<Void>> passwordRecovery(@Query("email") String email);

    @POST("/api/users/{id}/firebase_token")
    Call<Void> updateToken(@Path("id") Long userId, @Body FirebaseTokenUpdateDTO token);

    @POST("/api/users/{id}/firebase_token")
    Completable deleteToken(@Path("id") Long userId);

    @POST("/api/users/{id}/delete_account")
    Single<Response<Void>> deleteAccount(@Body PasswordDTO passwordDTO, @Path("id") Long userId);

    @Multipart
    @POST("/api/patients/{id}/profile_image/upload")
    Completable uploadProfilePicture(@Part MultipartBody.Part file, @Path("id") Long userId);

    @POST("/api/doctors/{id}/patients/sign-up")
    Single<Response<PatientDTO>> createPatientForDoctor(@Body CreatePatientDTO createPatientDTO,
                                              @Path("id") Long doctorId);

    @GET("/api/doctors/{id}")
    Single<DoctorDTO> getDoctorById(@Path("id") Long doctorId);


    @GET("/api/doctors/{id}/patients/info")
    Single<List<PatientInfo>> getAllPatientsInfoOfDoctor(@Path("id") Long doctorId);

    @GET("/api/patients/{id}/alerts")
    Single<List<Alert>> getAllAlertsOfPatient(@Path("id") Long patiendId);

    @POST("/api/patients/{id}/alerts/toggleSeenByDoctor")
    Completable toggleSeenByDoctor(@Path("id") Long patientId, @Query("alertId") Long alertId);

    @Multipart
    @POST("/api/diets/upload")
    Completable uploadDiet(@Part MultipartBody.Part file, @Query("patientId") Long patientId, @Query("filename") String filename);

    @POST("/api/patients/{id}/alerts/markAllAsSeen")
    Completable markAllAlertsAsSeenByDoctor(@Path("id") Long patientId);

    @POST("/api/doctors/{doctorId}/patients/{patientId}/add-goal")
    Single<WeightGoalDTO> addGoal(@Body AddWeightGoalDTO addWeightGoalDTO,
                                  @Path("doctorId") Long doctorId,
                                  @Path("patientId") Long patientId);

}
