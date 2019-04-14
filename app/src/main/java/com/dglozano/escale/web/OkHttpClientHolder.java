package com.dglozano.escale.web;


import javax.annotation.Nullable;

import okhttp3.OkHttpClient;

/**
 * This class was created to break a dependency cycle.
 * Check https://stackoverflow.com/questions/43914605/android-dagger2-okhttp-retrofit-dependency-cycle-error
 * for further details.
 */
public class OkHttpClientHolder {
    private OkHttpClient okHttpClient;

    @Nullable
    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public void setOkHttpClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }
}