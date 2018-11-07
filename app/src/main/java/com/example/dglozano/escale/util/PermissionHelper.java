package com.example.dglozano.escale.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.example.dglozano.escale.R;

import timber.log.Timber;

public class PermissionHelper {

    public static final int REQUEST_ENABLE_BT = 1;
    public static final int PERMISSION_REQUEST_COARSE = 2;

    public static boolean requestBluetoothPermission(final Activity activity) {
        final BluetoothManager bluetoothManager = (BluetoothManager) activity
                .getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = null;
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(activity, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            Timber.d("Device does not support Bluetooth 4.x");
            return false;
        }

        // Checks if Bluetooth is supported on the device.
        if (bluetoothAdapter == null) {
            Toast.makeText(activity, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            Timber.d("Device does not support Bluetooth at all");
            return false;
        }

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!bluetoothAdapter.isEnabled()) {
            Timber.d("Bluetooth is not Enabled. Preparing intent to activate it...");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        }

        // Ask for Coarse Location permission
        return requestCoarsePermission(activity);
    }

    public static boolean requestCoarsePermission(final Activity activity) {
        boolean alreadyGiven = ContextCompat
                .checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if (!alreadyGiven) {
            Timber.d("Coarse Location permission has not been given yet");
            askForPermission(Manifest.permission.ACCESS_COARSE_LOCATION, PERMISSION_REQUEST_COARSE,
                    activity.getResources().getString(R.string.coarse_permission_message), activity);
            return false;
        }
        Timber.d("Coarse Location permission has already been given");
        return true;
    }

    public static void askForPermission(final String manifestPermission, final int permissionCode,
                                        String rationaleMsgStr, final Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                manifestPermission)) {
            Timber.d("Permission for %1$s has been asked several times. Showing rationale msg...", manifestPermission);
            //If it had already asked many times for permission, it shows a different message
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(R.string.ask_permission_dialog_title);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setMessage(rationaleMsgStr);
            builder.setOnDismissListener(dialog -> activity.requestPermissions(
                    new String[]
                            {manifestPermission}
                    , permissionCode));
            builder.show();
        } else {
            Timber.d("Requestion permission for %1$s", manifestPermission);
            ActivityCompat.requestPermissions(activity, new String[]{manifestPermission}, permissionCode);
        }
    }
}

