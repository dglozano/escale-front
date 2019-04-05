package com.dglozano.escale.web;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import com.dglozano.escale.util.Constants;
import com.dglozano.escale.web.dto.LoginResponse;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Route;
import retrofit2.Call;
import timber.log.Timber;

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
        Timber.d("OkHttpAuthenticator starting method authenticate");
        if (response.code() == 401) {
            Timber.d("OkHttpAuthenticator - ERROR 401");
            String refreshToken = sharedPreferences.getString("refreshToken", null);
            String refreshTokenRequest = response.request().header("refreshToken");
            Timber.d("OkHttpAuthenticator - refreshToken in SharedPref %s", refreshToken);
            Timber.d("OkHttpAuthenticator - refreshToken in Request %s", refreshTokenRequest);
            if (refreshToken == null || refreshToken.equals(refreshTokenRequest)) {
                return null;
            }

            Call<LoginResponse> refreshCall = escaleRestApiHolder.getApiService().refreshToken(refreshToken);


            // TODO: Check if I have to change to async execution using enqueue() method instead.
            // According to the Authenticator contract, the operations here has to be sync apparently.
            retrofit2.Response<LoginResponse> refreshResponse = refreshCall.execute();

            if (refreshResponse != null && refreshResponse.code() == 200) {
                String newToken = refreshResponse.headers().get("token");
                String newRefreshToken = refreshResponse.headers().get("refreshToken");

                Timber.d("OkHttpAuthenticator - new token %s", newToken);
                Timber.d("OkHttpAuthenticator - new refreshToken %s", newRefreshToken);

                updateSharedPreferencesWithNewTokens(newToken, newRefreshToken);

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
        editor.putString(Constants.TOKEN_SHARED_PREF, accessToken);
        editor.putString(Constants.REFRESH_TOKEN_SHARED_PREF, refreshToken);
        editor.apply();
    }
}
