package com.dglozano.escale.web;

import javax.annotation.Nullable;

/**
 * This class was created to break a depency cycle.
 * Check https://stackoverflow.com/questions/43914605/android-dagger2-okhttp-retrofit-dependency-cycle-error
 * for further details.
 */
public class ApiServiceHolder {
    private EscaleRestApi apiService;

    @Nullable
    public EscaleRestApi getApiService() {
        return apiService;
    }

    public void setApiService(EscaleRestApi apiService) {
        this.apiService = apiService;
    }
}
