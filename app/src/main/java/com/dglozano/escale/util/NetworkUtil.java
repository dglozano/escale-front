package com.dglozano.escale.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.net.HttpURLConnection;
import java.net.URL;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class NetworkUtil {
    public static final int TYPE_WIFI = 1;
    public static final int TYPE_MOBILE = 2;
    public static final int TYPE_NOT_CONNECTED = 0;
    public static final int NETWORK_STATUS_NOT_CONNECTED = 0;
    public static final int NETWORK_STATUS_WIFI = 1;
    public static final int NETWORK_STATUS_MOBILE = 2;

    public static int getConnectivityStatus(Context context) {
        NetworkInfo activeNetwork = getActiveNetwork(context);
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public static int getConnectivityStatusString(Context context) {
        int conn = NetworkUtil.getConnectivityStatus(context);
        int status = NETWORK_STATUS_NOT_CONNECTED;
        if (conn == NetworkUtil.TYPE_WIFI) {
            status = NETWORK_STATUS_WIFI;
        } else if (conn == NetworkUtil.TYPE_MOBILE) {
            status = NETWORK_STATUS_MOBILE;
        }
        return status;
    }

    public static Single<Boolean> checkInternetAccess(Context context) {
        return Single.fromCallable(() -> {
            if (isNetworkAvailable(context)) {
                HttpURLConnection urlc = (HttpURLConnection)
                        (new URL("http://clients3.google.com/generate_204")
                                .openConnection());
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 204 &&
                        urlc.getContentLength() == 0);
            } else {
                Thread.sleep(1000);
                return false;
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo activeNetwork = getActiveNetwork(context);
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private static NetworkInfo getActiveNetwork(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm != null ? cm.getActiveNetworkInfo() : null;
    }
}
