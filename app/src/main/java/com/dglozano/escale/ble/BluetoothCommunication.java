package com.dglozano.escale.ble;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import timber.log.Timber;

import static com.dglozano.escale.ble.GattConstants.USER_CONTROL_POINT;
import static com.dglozano.escale.ble.GattConstants.USER_DATA_SERVICE;

public class BluetoothCommunication extends Service {

    public final static String ACTION_GATT_CONNECTED =
            "com.com.dglozano.escale.bluetooth.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.com.dglozano.escale.bluetooth.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_DATA_NOTIFICATION =
            "com.com.dglozano.escale.bluetooth.ACTION_DATA_NOTIFICATION";
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private static final long POST_DELAYED_WAIT = 60;
    private final Object lock = new Object();
    private IBinder mBinder = new LocalBinder();
    private Handler mHandler = new Handler();
    private BluetoothScanHelper mBluetoothScannerHelper = new BluetoothScanHelper();
    // Gatt server variables
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCustomCallback mGattCallback;
    private int mConnectionState = STATE_DISCONNECTED;
    // Syncrhonization of read/write over Bluetooth BLE
    private Queue<GattObjectValue<BluetoothGattDescriptor>> descriptorRequestQueue;
    private Queue<GattObjectValue<BluetoothGattCharacteristic>> characteristicRequestQueue;
    private CompletableFuture<BluetoothGattDescriptor> mWriteDescriptorFuture;
    private CompletableFuture<BluetoothGattCharacteristic> mReadResultFuture;
    private CompletableFuture<Integer> mOperationFuture;
    private boolean openRequest;

    /**
     * Convert a byte array to hex for debugging purpose
     *
     * @param data data we want to make human-readable (hex)
     * @return a human-readable string representing the content of 'data'
     */
    public static String byteInHex(byte[] data) {
        if (data == null) {
            return "";
        }

        if (data.length == 0) {
            return "";
        }

        final StringBuilder stringBuilder = new StringBuilder(3 * data.length);
        for (byte byteChar : data) {
            stringBuilder.append(String.format("%02X ", byteChar));
        }

        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    public CompletableFuture<BluetoothDevice> scanForBleDevices(String targetDeviceName) {
        return mBluetoothScannerHelper.scanForBleDevices(targetDeviceName);
    }

    public void connectGatt(BluetoothDevice device) {
        Timber.d("Connecting to [%s]", device.getAddress());

        mBluetoothGatt = device.connectGatt(
                this, false, mGattCallback);
    }

    /**
     * Bluetooth Communication
     */
    public List<BluetoothGattService> getBluetoothGattServices() {
        if (mBluetoothGatt == null) {
            return new ArrayList<>();
        }

        return mBluetoothGatt.getServices();
    }

    public boolean hasBluetoothGattService(UUID service) {
        return mBluetoothGatt != null && mBluetoothGatt.getService(service) != null;
    }

    /**
     * Write a byte array to a Bluetooth device.
     *
     * @param service        the Bluetooth UUID device service
     * @param characteristic the Bluetooth UUID characteristic
     * @param bytes          the bytes that should be written
     */
    public void writeBytes(UUID service, UUID characteristic, byte[] bytes) {
        synchronized (lock) {
            characteristicRequestQueue.add(
                    new GattObjectValue<>(
                            mBluetoothGatt.getService(service).getCharacteristic(characteristic),
                            bytes));
            handleRequests();
        }
    }

    /**
     * Read bytes from a Bluetooth device.
     * onBluetoothDataRead() will be triggered if read command was successful.
     *
     * @param service        the Bluetooth UUID device service
     * @param characteristic the Bluetooth UUID characteristic
     */
    public CompletableFuture<BluetoothGattCharacteristic> readBytes(UUID service, UUID characteristic) {
        mReadResultFuture = new CompletableFuture<>();

        BluetoothGattCharacteristic gattCharacteristic = mBluetoothGatt.getService(service)
                .getCharacteristic(characteristic);

        Timber.d("Read characteristic %s", characteristic);
        mBluetoothGatt.readCharacteristic(gattCharacteristic);

        return mReadResultFuture;
    }

    public CompletableFuture<BluetoothGattCharacteristic> readBytes(UUID service, UUID characteristic, UUID descriptor) {
        mReadResultFuture = new CompletableFuture<>();

        BluetoothGattDescriptor gattDescriptor = mBluetoothGatt.getService(service)
                .getCharacteristic(characteristic).getDescriptor(descriptor);

        Timber.d("Read descriptor %s", descriptor);
        mBluetoothGatt.readDescriptor(gattDescriptor);

        return mReadResultFuture;
    }

    /**
     * Set indication flag on for the Bluetooth device.
     *
     * @param service        the Bluetooth UUID device service
     * @param characteristic the Bluetooth UUID characteristic
     */
    public CompletableFuture<BluetoothGattDescriptor> setIndicationOn(UUID service, UUID characteristic, UUID descriptor) {
        mWriteDescriptorFuture = new CompletableFuture<>();

        Timber.d("Set indication on for %s", characteristic);

        try {
            BluetoothGattCharacteristic gattCharacteristic =
                    mBluetoothGatt.getService(service).getCharacteristic(characteristic);
            mBluetoothGatt.setCharacteristicNotification(gattCharacteristic, true);

            synchronized (lock) {
                descriptorRequestQueue.add(
                        new GattObjectValue<>(
                                gattCharacteristic.getDescriptor(descriptor),
                                BluetoothGattDescriptor.ENABLE_INDICATION_VALUE));
                handleRequests();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mWriteDescriptorFuture;
    }

    /**
     * Set notification flag on for the Bluetooth device.
     *
     * @param service        the Bluetooth UUID device service
     * @param characteristic the Bluetooth UUID characteristic
     */
    public CompletableFuture<BluetoothGattDescriptor> setNotificationOn(UUID service, UUID characteristic, UUID descriptor) {
        mWriteDescriptorFuture = new CompletableFuture<>();

        Timber.d("Set notification on for %s", characteristic);

        try {
            BluetoothGattCharacteristic gattCharacteristic =
                    mBluetoothGatt.getService(service).getCharacteristic(characteristic);
            mBluetoothGatt.setCharacteristicNotification(gattCharacteristic, true);

            synchronized (lock) {
                descriptorRequestQueue.add(
                        new GattObjectValue<>(
                                gattCharacteristic.getDescriptor(descriptor),
                                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE));
                handleRequests();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mWriteDescriptorFuture;
    }

    /**
     * Set notification flag off for the Bluetooth device.
     *
     * @param service        the Bluetooth UUID device service
     * @param characteristic the Bluetooth UUID characteristic
     */
    public CompletableFuture<BluetoothGattDescriptor> setNotificationOff(UUID service, UUID characteristic, UUID descriptor) {
        mWriteDescriptorFuture = new CompletableFuture<>();

        Timber.d("Set notification off for %s", characteristic);

        BluetoothGattCharacteristic gattCharacteristic =
                mBluetoothGatt.getService(service).getCharacteristic(characteristic);
        mBluetoothGatt.setCharacteristicNotification(gattCharacteristic, false);

        synchronized (lock) {
            descriptorRequestQueue.add(
                    new GattObjectValue<>(
                            gattCharacteristic.getDescriptor(descriptor),
                            BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE));
            handleRequests();
        }
        return mWriteDescriptorFuture;
    }

    private void handleRequests() {
        synchronized (lock) {
            // check for pending request
            if (openRequest) {
                Timber.d("Request pending (queue %d %d)",
                        descriptorRequestQueue.size(), characteristicRequestQueue.size());
                return; // yes, do nothing
            }

            // handle descriptor requests first
            GattObjectValue<BluetoothGattDescriptor> descriptor = descriptorRequestQueue.poll();
            if (descriptor != null) {
                descriptor.gattObject.setValue(descriptor.value);

                Timber.d("Write descriptor %s: %s (queue: %d %d)",
                        descriptor.gattObject.getUuid(), byteInHex(descriptor.gattObject.getValue()),
                        descriptorRequestQueue.size(), characteristicRequestQueue.size());
                if (!mBluetoothGatt.writeDescriptor(descriptor.gattObject)) {
                    Timber.d("Failed to initiate write of descriptor %s",
                            descriptor.gattObject.getUuid());
                }
                openRequest = true;
                return;
            }

            // handle characteristics requests second
            GattObjectValue<BluetoothGattCharacteristic> characteristic = characteristicRequestQueue.poll();
            if (characteristic != null) {
                characteristic.gattObject.setValue(characteristic.value);

                Timber.d("Write characteristic %s: %s (queue: %d %d)",
                        characteristic.gattObject.getUuid(), byteInHex(characteristic.gattObject.getValue()),
                        descriptorRequestQueue.size(), characteristicRequestQueue.size());
                if (!mBluetoothGatt.writeCharacteristic(characteristic.gattObject)) {
                    Timber.d("Failed to initiate write of characteristic %s",
                            characteristic.gattObject.getUuid());
                }
                openRequest = true;
            }
        }
    }

    @Override
    public void onCreate() {
        Timber.d("onCreate");

        mGattCallback = new BluetoothGattCustomCallback();
        mBluetoothGatt = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Timber.d("onBind");
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Timber.d("onDestroy");
        disconnect();
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Timber.d("onUnbind");
        mBluetoothScannerHelper.stopScanningForBleDevices(new Exception("Service unbinded"));
        disconnect();
        return super.onUnbind(intent);
    }

    public void disconnect() {
        Timber.d("Disconnecting from Gatt Server");
        if (mBluetoothGatt == null)
            return;
        mBluetoothGatt.disconnect();
        mBluetoothGatt = null;
    }

    public CompletableFuture<Integer> createUser(byte[] pin) {
        mOperationFuture = new CompletableFuture<>();
        byte[] bytesCreate = {0x01, pin[0], pin[1]}; // create user, PIN 0x1000
        writeBytes(USER_DATA_SERVICE, USER_CONTROL_POINT, bytesCreate);

        return mOperationFuture;
    }

    public CompletableFuture<Integer> deleteUser(byte index) {
        mOperationFuture = new CompletableFuture<>();
        byte[] bytesDelete = {0x03, index}; // create user, PIN 0x1000
        writeBytes(USER_DATA_SERVICE, USER_CONTROL_POINT, bytesDelete);

        return mOperationFuture;
    }

    public CompletableFuture<Integer> consentUser(byte index, byte[] pin) {
        mOperationFuture = new CompletableFuture<>();
        byte[] bytesConsent = {0x02, index, pin[0], pin[1]}; // create user, PIN 0x1000
        writeBytes(USER_DATA_SERVICE, USER_CONTROL_POINT, bytesConsent);

        return mOperationFuture;
    }

    private void broadcastUpdate(final String action) {
        Timber.d("Sending intent %1$s", action);
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    // Representation of a GattObject plus its bytes
    private class GattObjectValue<GattObject> {
        public final GattObject gattObject;
        public final byte[] value;

        public GattObjectValue(GattObject gattObject, byte[] value) {
            this.gattObject = gattObject;
            this.value = value;
        }
    }

    public class LocalBinder extends Binder {
        public BluetoothCommunication getService() {
            return BluetoothCommunication.this;
        }
    }

    protected class BluetoothGattCustomCallback extends BluetoothGattCallback {

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            Timber.d("onConnectionStateChange: status=%d, newState=%d", status, newState);
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    // Empty
                }

                if (!gatt.discoverServices()) {
                    Timber.d("Could not start service discovery");
                    disconnect();
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Timber.d("Disconnected from GATT server");
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            Timber.d("onServicesDiscovered: status=%d (%d services)",
                    status, gatt.getServices().size());

            mConnectionState = STATE_CONNECTED;
            broadcastUpdate(ACTION_GATT_CONNECTED);

            synchronized (lock) {
                // Clear from possible previous setups
                characteristicRequestQueue = new LinkedList<>();
                descriptorRequestQueue = new LinkedList<>();
                openRequest = false;
            }

            try {
                // Sleeping a while after discovering services fixes connection problems.
                // See https://github.com/NordicSemiconductor/Android-DFU-Library/issues/10
                // for some technical background.
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void postDelayedHandleRequests() {
            // Wait a short while before starting the next operation as suggested
            // on the android.jlelse.eu link above.
            mHandler.postDelayed(() -> {
                synchronized (lock) {
                    openRequest = false;
                    handleRequests();
                }
            }, POST_DELAYED_WAIT);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor,
                                      int status) {
            mWriteDescriptorFuture.complete(descriptor);
            postDelayedHandleRequests();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
            postDelayedHandleRequests();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Timber.d("onCharacteristicRead %s (status=%d): %s",
                    characteristic.getUuid(), status, byteInHex(characteristic.getValue()));

            synchronized (lock) {
                mReadResultFuture.complete(characteristic);
                postDelayedHandleRequests();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Timber.d("onCharacteristicChanged %s: %s",
                    characteristic.getUuid(), byteInHex(characteristic.getValue()));

            synchronized (lock) {
                mOperationFuture.complete(1);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt,
                                     BluetoothGattDescriptor descriptor,
                                     int status) {
            Timber.d("onDescriptorRead %s (status=%d): %s",
                    descriptor.getUuid(), status, byteInHex(descriptor.getValue()));

            postDelayedHandleRequests();
        }
    }
}
