package com.dglozano.escale.web;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.dglozano.escale.web.dto.LoginResponse;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Route;
import retrofit2.Call;

public class CustomOkHttpAuthenticator implements Authenticator {

    private ApiServiceHolder escaleRestApiHolder;
    private SharedPreferences sharedPreferences;

    @Inject
    public CustomOkHttpAuthenticator(ApiServiceHolder escaleRestApiHolder, SharedPreferences sharedPreferences) {
        this.escaleRestApiHolder = escaleRestApiHolder;
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public Request authenticate(@NonNull Route route, @NonNull okhttp3.Response response) throws IOException {
        if (escaleRestApiHolder.getApiService() == null) {
            return null;
        }
        if (response.code() == 401) {
            String refreshToken = sharedPreferences.getString("refreshToken", null);

            if (refreshToken == null) {
                return null;
            }

            Call<LoginResponse> refreshCall = escaleRestApiHolder.getApiService().refreshToken(refreshToken);


            // TODO: Check if I have to change to async execution using enqueue() method instead.
            // According to the Authenticator contract, the operations here has to be sync apparently.
            retrofit2.Response<LoginResponse> refreshResponse = refreshCall.execute();

            if (refreshResponse != null && refreshResponse.code() == 200) {
                String newToken = refreshResponse.headers().get("token");
                String newRefreshToken = refreshResponse.headers().get("refreshToken");

                updateSharedPreferencesWithNewTokens(newToken, refreshToken);

                return response.request().newBuilder()
                            .header("token", newToken)
                            .build();
            } else {
                return null;
            }
        }
        return null;
    }

    private void updateSharedPreferencesWithNewTokens(String accessToken, String refreshToken) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", accessToken);
        editor.putString("refreshToken", refreshToken);
        editor.apply();
    }
}
