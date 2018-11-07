package com.example.dglozano.escale.utils;

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

/**
 * Created by dglozano on 29/04/18.
 */

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
            return false;
        }

        // Checks if Bluetooth is supported on the device.
        if (bluetoothAdapter == null) {
            Toast.makeText(activity, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            return false;
        }

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        }

        // Pedir el permiso de ubicacion y si no lo tiene pedirlo
        return requestCoarsePermission(activity);
    }

    public static boolean requestCoarsePermission(final Activity activity) {
        boolean alreadyGiven = ContextCompat
                .checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if (!alreadyGiven) {
            askForPermission(Manifest.permission.ACCESS_COARSE_LOCATION, PERMISSION_REQUEST_COARSE,
                    activity.getResources().getString(R.string.coarse_permission_message), activity);
            return false;
        }
        return true;
    }

    public static void askForPermission(final String permisoManifest, final int codigoPermiso,
                                        String rationaleMsgStr, final Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                permisoManifest)) {
            //If it had already asked many times for permission, it show a different message
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(R.string.ask_permission_dialog_title);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setMessage(rationaleMsgStr);
            builder.setOnDismissListener(dialog -> activity.requestPermissions(
                    new String[]
                            {permisoManifest}
                    , codigoPermiso));
            builder.show();
        } else {
            // Abre el dialogo para pedir el permiso de la ubicacion.
            ActivityCompat.requestPermissions(activity, new String[]{permisoManifest}, codigoPermiso);
        }
    }
}

