package com.dglozano.escale.web;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HeaderTokenInterceptor implements Interceptor {

    private SharedPreferences sharedPreferences;

    @Inject
    public HeaderTokenInterceptor(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        String token = sharedPreferences.getString("token", "");
        Request request = chain.request();
        if (request.url().encodedPath().contains("auth/login")
                || request.url().encodedPath().contains("auth/refresh")) {
            return chain.proceed(request);
        }

        Request newRequest = request.newBuilder()
                .addHeader("token", token)
                .build();

        return chain.proceed(newRequest);
    }
}