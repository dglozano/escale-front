package com.dglozano.escale.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dglozano.escale.ui.common.NoInternetActivity;

import timber.log.Timber;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        int status = NetworkUtil.getConnectivityStatusString(context);
        Timber.d("Network status change %s", status);
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                Intent newIntent = new Intent(context, NoInternetActivity.class);
                context.startActivity(newIntent);
                if (context instanceof Activity) {
                    ((Activity) context).finish();
                }
            }
        }
    }
}