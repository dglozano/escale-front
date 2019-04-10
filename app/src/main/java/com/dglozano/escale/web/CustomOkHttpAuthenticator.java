package com.dglozano.escale.web;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

import com.dglozano.escale.ui.login.LoginActivity;
import com.dglozano.escale.util.Constants;
import com.dglozano.escale.util.LogoutHelper;
import com.dglozano.escale.web.dto.LoginResponse;

import java.io.IOException;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Route;
import retrofit2.Call;
import timber.log.Timber;

import static com.dglozano.escale.ui.main.MainActivity.ASK_NEW_FIREBASE_TOKEN;

public class CustomOkHttpAuthenticator implements Authenticator {

    private ApiServiceHolder escaleRestApiHolder;
    private SharedPreferences sharedPreferences;
    private LogoutHelper logoutHelper;

    @Inject
    public CustomOkHttpAuthenticator(ApiServiceHolder escaleRestApiHolder,
                                     SharedPreferences sharedPreferences,
                                     LogoutHelper logoutHelper) {
        this.escaleRestApiHolder = escaleRestApiHolder;
        this.sharedPreferences = sharedPreferences;
        this.logoutHelper = logoutHelper;
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
            if (refreshToken == null || refreshToken.equals(refreshTokenRequest)) {
                // If the refresh token is not present or it was used in the previous request and
                // failed again (it was expired, or didn't match the JTI), then logout user
                logoutHelper.logout();

                return null;
            }

            Call<LoginResponse> refreshCall = escaleRestApiHolder.getApiService().refreshToken(refreshToken);

            // According to the Authenticator contract, the operations here has to be sync apparently.
            retrofit2.Response<LoginResponse> refreshResponse = refreshCall.execute();

            if (refreshResponse != null && refreshResponse.code() == 200) {
                String newToken = refreshResponse.headers().get("token");
                String newRefreshToken = refreshResponse.headers().get("refreshToken");

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
