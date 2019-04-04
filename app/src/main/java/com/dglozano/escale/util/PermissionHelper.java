package com.dglozano.escale.util;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

public class PermissionHelper {

    private static final int REQUEST_PERMISSION_COARSE_LOCATION = 9358;
    private static final int REQUEST_EXTERNAL_STORAGE = 1234;


    private PermissionHelper() {
        // Utility class
    }

    public static boolean checkLocationPermissionGranted(final Context context) {
        return isPermissionGranted(context, permission.ACCESS_COARSE_LOCATION);
    }

    public static void requestLocationPermissionInFragment(final Fragment fragment) {
        requestPermission(fragment, permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
    }

    public static boolean isRequestLocationPermissionGranted(final int requestCode,
                                                             final String[] permissions,
                                                             final int[] grantResults) {
        return isPermissionGranted(requestCode, permissions, grantResults,
                permission.ACCESS_COARSE_LOCATION);
    }


    public static void requestExternalStoragePermission(final Activity activity) {
        requestPermission(activity, permission.READ_EXTERNAL_STORAGE, REQUEST_EXTERNAL_STORAGE);
    }

    public static boolean isPermissionGranted(final Context context, String permissionToCheck) {
        return ContextCompat.checkSelfPermission(context, permissionToCheck)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isPermissionGranted(final int requestCode,
                                              final String[] permissions,
                                              final int[] grantResults,
                                              final String permissionToCheck) {
        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(permissionToCheck)
                    && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }

        return false;
    }

    private static void requestPermission(final Activity activity, String permissionName, Integer code) {
        activity.requestPermissions(
                new String[]{permissionName},
                code);
    }

    private static void requestPermission(final Fragment fragment, String permissionName, Integer code) {
        fragment.requestPermissions(
                new String[]{permissionName},
                code);
    }
}

