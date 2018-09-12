package com.example.dglozano.escale.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BluetoothScanHelper {

    private static final String TAG = BluetoothScanHelper.class.getSimpleName();

    // Scanning variables
    private static final long SCAN_PERIOD = 10 * 1000;
    private boolean mScanning;
    private String targetDeviceName;
    private Handler mScanPeriodHandler;
    private ScanCallback mLeScanCallback;
    private BluetoothAdapter mBluetoothAdapter;
    private CompletableFuture<BluetoothDevice> mScanResultFuture;

    public BluetoothScanHelper() {
        mScanning = false;
        mScanPeriodHandler = new Handler();
        mLeScanCallback = new MyScanCallback();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    protected CompletableFuture<BluetoothDevice> scanForBleDevices(String targetDeviceName) {
        this.targetDeviceName = targetDeviceName;
        mScanResultFuture = new CompletableFuture<>();

        final BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        // Stops scanning after a pre-defined scan period.
        mScanPeriodHandler.postDelayed(() -> {
            if(mScanning) {
                mScanning = false;
                bluetoothLeScanner.stopScan(mLeScanCallback);
                Log.d(TAG, "Stop Scanning after 10 seconds");
                mScanResultFuture.completeExceptionally(new Exception("Device not found after 10 seconds"));
            }
        }, SCAN_PERIOD);
        mScanning = true;
        Log.d(TAG, "Scanning");
        bluetoothLeScanner.startScan(mLeScanCallback);
        return mScanResultFuture;
    }

    protected void stopScanningForBleDevices() {
        final BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mScanning = false;
        bluetoothLeScanner.stopScan(mLeScanCallback);
    }

    private class MyScanCallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            final BluetoothDevice device = result.getDevice();
            if(device != null
                    && device.getName() != null
                    && device.getName().toString().contains(targetDeviceName)) {
                stopScanningForBleDevices();
                Log.d(TAG, "Stop scanning, found " + device.getName());
                mScanResultFuture.complete(device);
            } /*else {
                Log.d(TAG, "Stop Scanning after founding null device");
                //TODO: STRING RESOURCE
                mScanResultFuture.completeExceptionally(new Exception("Found null device"));
            }*/
            mScanning = false;
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.d(TAG, "onBatchScanResults " + results.size());
            mScanning = false;
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d(TAG, "OnScanFailed - errorCode: " + errorCode);
            mScanning = false;
            super.onScanFailed(errorCode);
        }
    }
}
