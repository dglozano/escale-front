package com.dglozano.escale.ble;

import android.app.Service;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.dglozano.escale.R;
import com.dglozano.escale.di.annotation.ApplicationScope;
import com.dglozano.escale.di.annotation.BluetoothInfo;
import com.dglozano.escale.util.HexString;
import com.dglozano.escale.util.ScanExceptionHandler;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.exceptions.BleScanException;
import com.polidea.rxandroidble2.internal.RxBleLog;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

@ApplicationScope
public class BleCommunicationService extends Service {

    @Inject
    RxBleClient rxBleClient;
    @Inject
    @BluetoothInfo
    String mScaleBleNameString;

    private IBinder mBinder;
    private Disposable mScanDisposable;
    private Disposable mConnectionStateDisposable;
    private Disposable mConnectionDisposable;
    private CompositeDisposable mCompositeDisposable;
    private MutableLiveData<Boolean> mIsScanning;
    private MutableLiveData<Boolean> mIsConnecting;
    private MutableLiveData<Boolean> mIsConnectedToScale;
    private MediatorLiveData<Boolean> mIsScanningOrConnecting;
    private Observable<RxBleConnection> mConnectionObservable;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
        Timber.d("onCreate.");
        RxBleClient.setLogLevel(RxBleLog.VERBOSE);
        mBinder = new LocalBinder();
        mCompositeDisposable = new CompositeDisposable();
        mIsScanning = new MutableLiveData<>();
        mIsConnectedToScale = new MutableLiveData<>();
        mIsConnecting = new MutableLiveData<>();
        mIsConnectedToScale.setValue(false);
        mIsScanning.setValue(false);
        mIsConnecting.setValue(false);
        mIsScanningOrConnecting = new MediatorLiveData<>();
        prepareScanningOrConnectionMediator();
    }

    private void prepareScanningOrConnectionMediator() {
        mIsScanningOrConnecting.addSource(mIsScanning, isScanning -> {
            if (mIsConnecting.getValue() || isScanning) {
                mIsScanningOrConnecting.postValue(true);
            } else {
                mIsScanningOrConnecting.postValue(false);
            }
        });
        mIsScanningOrConnecting.addSource(mIsConnecting, isConnecting -> {
            if (mIsScanning.getValue() || isConnecting) {
                mIsScanningOrConnecting.postValue(true);
            } else {
                mIsScanningOrConnecting.postValue(false);
            }
        });
    }

    public void scanBleDevices() {
        Timber.d("start scanning.");
        mScanDisposable = rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build(),
                new ScanFilter.Builder()
                        .setDeviceName(mScaleBleNameString)
                        .build()
        )
                .timeout(10, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(this::disposeScanning)
                .subscribe(this::connectToBleDevice, this::throwException);
        mIsScanning.postValue(true);
    }

    private void connectToBleDevice(ScanResult scanResult) {
        mIsConnecting.postValue(true);
        disposeScanning();
        RxBleDevice scaleDevice = scanResult.getBleDevice();
        Timber.d("Found %1$s device. MAC Address: %2$s. Trying to connect...",
                scaleDevice.getName(),
                scaleDevice.getMacAddress());
        if (mConnectionStateDisposable != null) {
            mConnectionStateDisposable.dispose();
            mConnectionStateDisposable = null;
        }
        mConnectionStateDisposable = scaleDevice.observeConnectionStateChanges()
                .subscribe(this::onConnectionStateChanged, this::throwException);

        mConnectionDisposable =
                BondingHelper.bondWithDevice(this, scaleDevice, 30, TimeUnit.SECONDS)
                        .andThen(scaleDevice.establishConnection(false))
                        .observeOn(AndroidSchedulers.mainThread())
                        .doFinally(this::disposeConnection)
                        .subscribe(this::communicationInitialization, this::throwException);
    }

    private void throwException(Throwable throwable) {
        Timber.d("throwException(). Message: %1$s", throwable.getMessage());
        if (throwable instanceof BleScanException) {
            ScanExceptionHandler.handleException(this, (BleScanException) throwable);
        } else if (throwable instanceof TimeoutException) {
            Toast.makeText(this, R.string.ble_scan_timeout, Toast.LENGTH_SHORT).show();
        } else {
            Timber.e(throwable);
            Toast.makeText(this, R.string.ble_error_try_again, Toast.LENGTH_SHORT).show();
        }
        mIsScanning.postValue(false);
        mIsConnecting.postValue(false);
    }

    private void disposeScanning() {
        Timber.d("Disposing Scanning.");
        if (mScanDisposable != null) {
            mScanDisposable.dispose();
        }
        mScanDisposable = null;
        mIsScanning.postValue(false);
    }

    public void disposeConnection() {
        Timber.d("Disposing Connection.");
        if (mConnectionDisposable != null) {
            mConnectionDisposable.dispose();
        }
        mConnectionDisposable = null;
        mIsConnectedToScale.postValue(false);
        mIsConnecting.postValue(false);
    }

    private void onConnectionStateChanged(RxBleConnection.RxBleConnectionState rxBleConnectionState) {
        Timber.d("ConnectionState changed. New state: %1$s", rxBleConnectionState.toString());
        if (rxBleConnectionState.equals(RxBleConnection.RxBleConnectionState.CONNECTED)) {
            mIsScanning.postValue(false);
            mIsConnecting.postValue(false);
            mIsConnectedToScale.postValue(true);
        } else {
            mIsConnectedToScale.postValue(false);
        }
    }


    private Single<byte[]> prepareObservableToWriteCharacteristic(
            UUID characteristicUuid,
            String hexString,
            RxBleConnection connection) {
        return connection.writeCharacteristic(characteristicUuid, getInputBytes(hexString))
                .doOnSuccess(notificationObservable ->
                        Timber.d("Characteristic %1$s written with bytes %2$s",
                                characteristicUuid.toString(),
                                hexString));
    }

    private Single<byte[]> prepareObservableToReadCharacteristic(
            UUID characteristicUuid,
            RxBleConnection connection) {
        return connection.readCharacteristic(characteristicUuid)
                .doOnSuccess(notificationObservable ->
                        Timber.d("Characteristic %1$s read", characteristicUuid.toString()))
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<byte[]> prepareObservableToSetNotification(
            UUID characteristicUuid,
            RxBleConnection connection) {
        return connection.setupNotification(characteristicUuid)
                .doOnNext(notificationObservable ->
                        Timber.d(
                                "Notification enabled in characteristic %1$s",
                                characteristicUuid.toString()))
                .flatMap(notificationObservable -> notificationObservable)
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<byte[]> prepareObservableToSetIndication(
            UUID characteristicUuid,
            RxBleConnection connection) {
        return connection.setupIndication(characteristicUuid)
                .doOnNext(notificationObservable ->
                        Timber.d(
                                "Indication enabled in characteristic %1$s",
                                characteristicUuid.toString()))
                .flatMap(notificationObservable -> notificationObservable)
                .observeOn(AndroidSchedulers.mainThread());
    }


    public void communicationInitialization(RxBleConnection rxBleConnection) {
        List<UUID> indicationCharacteristics = new ArrayList<>();
        indicationCharacteristics.add(GattConstants.CLIENT_CHARACTERISTICS_CONFIGURATION);
        indicationCharacteristics.add(GattConstants.BODY_COMPOSITION_MEASUREMENT);

        /*
        indicationCharacteristics.stream()
                .map(uuid -> prepareObservableToSetIndication(uuid, rxBleConnection))
                .map(observable -> observable.observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::onNotificationReceived, this::onNotificationSetupFailure))
                .map(mCompositeDisposable::add);



        List<UUID> notificationCharacteristics = new ArrayList<>();
        notificationCharacteristics.add(GattConstants.DB_CHANGE_INCREMENT);
        notificationCharacteristics.add(GattConstants.CUSTOM_FFF2_USER_LIST_CHARACTERISTIC);
        notificationCharacteristics.add(GattConstants.CURRENT_TIME);

        notificationCharacteristics.stream()
                .map(uuid -> prepareObservableToSetNotification(uuid, mConnectionObservable))
                .map(observable -> observable.observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::onNotificationReceived, this::onNotificationSetupFailure))
                .map(mCompositeDisposable::add);
                */

        Observable<byte[]> indicationsObservable = prepareObservableToSetIndication(
                GattConstants.USER_CONTROL_POINT, rxBleConnection);


        String hex1 = "012348"; // create user with pin 2348 (9032 o 3290 en DEC)
        Single<byte[]> writeCreateUser = prepareObservableToWriteCharacteristic(
                GattConstants.USER_CONTROL_POINT,
                hex1, rxBleConnection);

        String hex2 = "02012348";
        Single<byte[]> writeConsentUser = prepareObservableToWriteCharacteristic(
                GattConstants.USER_CONTROL_POINT,
                hex2, rxBleConnection);


        prepareObservableToSetIndication(GattConstants.USER_CONTROL_POINT, rxBleConnection)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bytes -> Timber.d("Success 1 %1$s",
                        HexString.bytesToHex(bytes)),
                        throwable -> Timber.e(throwable, "Error"));

        rxBleConnection.writeCharacteristic(GattConstants.USER_CONTROL_POINT, getInputBytes(hex1))
                .observeOn(AndroidSchedulers.mainThread())
                .retry()
                .subscribe(bytes -> Timber.d("Success 2 %1$s",
                        HexString.bytesToHex(bytes)),
                        throwable -> Timber.e(throwable, "Error"));

        /*
        writeCreateUser
                .flatMap(bytes -> indicationsObservable.firstOrError())
                .flatMap(bytes -> {
                    Timber.d("bytes %1$s", bytes);
                    return writeConsentUser;
                })
                .doOnSuccess(bytes -> indicationsObservable.firstOrError())
                .doOnSuccess(bytes -> Timber.d("bytes read after contest %1$s", bytes))*/
    }

    private void onNotificationReceived(byte[] value) {
        Timber.d("Read value: %1$s", HexString.bytesToHex(value));
    }

    private void onNotificationSetupFailure(Throwable throwable) {
        Timber.e(throwable, "Error setting up notification.");
    }

    public MutableLiveData<Boolean> isScanning() {
        return mIsScanning;
    }

    public MutableLiveData<Boolean> isConnecting() {
        return mIsConnecting;
    }

    public MutableLiveData<Boolean> isConnectedToScale() {
        return mIsConnectedToScale;
    }

    public MediatorLiveData<Boolean> isScanningOrConnecting() {
        return mIsScanningOrConnecting;
    }

    @SuppressWarnings("unused")
    private void onConnectionReceived(RxBleConnection rxBleConnection) {
        Timber.d("Connection received.");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Timber.d("onBind(). Returning binder...");
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Timber.d("onDestroy(). Disposing connection and scanning...");
        disposeConnection();
        disposeScanning();
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Timber.d("onUnbind().");
        return super.onUnbind(intent);
    }

    public class LocalBinder extends Binder {
        public BleCommunicationService getService() {
            return BleCommunicationService.this;
        }
    }

    private byte[] getInputBytes(String hexString) {
        return HexString.hexToBytes(hexString);
    }
}
