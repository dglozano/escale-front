package com.dglozano.escale.ble;

import android.app.Service;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.dglozano.escale.R;
import com.dglozano.escale.util.Constants;
import com.dglozano.escale.util.ui.Event;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.exceptions.BleDisconnectedException;
import com.polidea.rxandroidble2.exceptions.BleScanException;
import com.polidea.rxandroidble2.internal.RxBleLog;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.SingleSubject;
import timber.log.Timber;

import static com.dglozano.escale.ble.CommunicationHelper.bytesToHex;
import static com.dglozano.escale.ble.CommunicationHelper.hexToBytes;

public abstract class BaseBleService extends Service {

    protected RxBleConnection mRxBleConnection;
    protected Disposable mScanDisposable;
    protected Disposable mConnectionStateDisposable;
    protected Disposable mConnectionDisposable;
    protected MutableLiveData<Boolean> mIsScanning;
    protected MutableLiveData<Event<Integer>> mErrorEvent;
    protected MutableLiveData<String> mConnectionState;
    protected MediatorLiveData<Boolean> mIsScanningOrConnecting;
    protected RxBleDevice mScaleDevice;
    private SingleSubject<String> mDisconnectedEventSubject;
    private boolean isReconnecting = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("onCreate.");
        RxBleClient.setLogLevel(RxBleLog.VERBOSE);

        mIsScanning = new MutableLiveData<>();
        mIsScanning.setValue(false);

        mConnectionState = new MutableLiveData<>();
        mConnectionState.setValue(Constants.DISCONNECTED);

        mErrorEvent = new MutableLiveData<>();

        mIsScanningOrConnecting = new MediatorLiveData<>();
        prepareScanningOrConnectionMediator();
    }

    protected void scanBleDevices(RxBleClient rxBleClient, Consumer<? super ScanResult> onScanResult) {
        scanBleDevices(rxBleClient, onScanResult, null);
    }

    protected void scanBleDevices(RxBleClient rxBleClient, Consumer<? super ScanResult> onScanResult, String nameFilter) {
        Timber.d("Start scanning.");
        Observable<ScanResult> observableScanResults = nameFilter == null || nameFilter.isEmpty() ?
                rxBleClient.scanBleDevices(new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build()) :
                rxBleClient.scanBleDevices(new ScanSettings.Builder()
                                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                                .build(),
                        new ScanFilter.Builder()
                                .setDeviceName(nameFilter)
                                .build());

        mScanDisposable = observableScanResults
                .timeout(10, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .doFinally(this::disposeScanning)
                .subscribe(onScanResult, this::throwException);
        mConnectionState.postValue(Constants.SCANNING);
        mIsScanning.postValue(true);
    }

    private void prepareScanningOrConnectionMediator() {
        mIsScanningOrConnecting.addSource(mIsScanning, isScanning -> {
            boolean connected = mConnectionState.getValue().equals(Constants.CONNECTED);
            boolean disconnected = mConnectionState.getValue().equals(Constants.DISCONNECTED);
            boolean isConnecting = !connected && !disconnected;
            if (isConnecting || isScanning) {
                mIsScanningOrConnecting.postValue(true);
            } else {
                mIsScanningOrConnecting.postValue(false);
            }
        });
        mIsScanningOrConnecting.addSource(mConnectionState, state -> {
            boolean connected = state.equals(Constants.CONNECTED);
            boolean disconnected = state.equals(Constants.DISCONNECTED);
            boolean isConnecting = !connected && !disconnected;
            if (mIsScanning.getValue() || isConnecting) {
                mIsScanningOrConnecting.postValue(true);
            } else {
                mIsScanningOrConnecting.postValue(false);
            }
        });
    }

    protected Observable<RxBleConnection> connectToBleDevice(ScanResult scanResult) {
        mConnectionState.postValue(Constants.BONDING);
        disposeScanning();
        mScaleDevice = scanResult.getBleDevice();
        Timber.d("Found %1$s device. MAC Address: %2$s. Trying to connect...",
                mScaleDevice.getName(),
                mScaleDevice.getMacAddress());
        if (mConnectionStateDisposable != null) {
            mConnectionStateDisposable.dispose();
            mConnectionStateDisposable = null;
        }
        mConnectionStateDisposable = mScaleDevice.observeConnectionStateChanges()
                .subscribe(this::onConnectionStateChanged, this::throwException);

        return BondingHelper.bondWithDevice(this, mScaleDevice, 15, TimeUnit.SECONDS)
                .andThen(Completable.fromAction(() -> mConnectionState.postValue(Constants.CONNECTING)))
                .andThen(createConnectionObservable(mScaleDevice))
                .flatMap(rxBleConnection -> {
                    rxBleConnection.requestMtu(50);
                    return Observable.just(rxBleConnection);
                })
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io());
    }

    protected Observable<RxBleConnection> reconnectToBleDevice() {
        disposeConnectionAndWaitPrepareForReconnect();
        return mDisconnectedEventSubject
                .delay(500, TimeUnit.MILLISECONDS)
                .flatMapCompletable(ignore -> {
                    Timber.d("About to bond after reconnection and waiting");
                    return BondingHelper.bondWithDevice(this, mScaleDevice, 15, TimeUnit.SECONDS);
                })
                .andThen(createConnectionObservable(mScaleDevice, true))
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io());
    }

    protected Observable<RxBleConnection> createConnectionObservable(RxBleDevice scaleDevice) {
        return createConnectionObservable(scaleDevice, false);
    }

    protected Observable<RxBleConnection> createConnectionObservable(RxBleDevice scaleDevice, boolean autoConnect) {
        return scaleDevice
                .establishConnection(autoConnect)
                .observeOn(Schedulers.io())
                .doOnError(this::throwException);
    }

    protected void throwException(Throwable throwable) {
        Timber.d("throwException(). Message: %1$s", throwable.getMessage());
        Integer errorStrResource;
        if (throwable instanceof BleScanException) {
            errorStrResource = ScanExceptionHandler.handleException(this, (BleScanException) throwable);
        } else if (throwable instanceof TimeoutException) {
            errorStrResource = R.string.ble_scan_timeout;
        } else if (throwable instanceof CommunicationHelper.LoginScaleUserFailed) {
            errorStrResource = R.string.ble_scale_login_error;
        } else if (throwable instanceof BleDisconnectedException) {
            errorStrResource = R.string.ble_scale_disconnected;
        } else if (throwable instanceof CommunicationHelper.ScaleUserLimitExcedded) {
            errorStrResource = R.string.ble_scale_user_limit_error;
        } else if (throwable instanceof CommunicationHelper.DeleteScaleUserFailed) {
            errorStrResource = R.string.ble_scale_delete_error;
        } else {
            Timber.e(throwable);
            errorStrResource = R.string.ble_error_try_again;
        }

        mErrorEvent.postValue(new Event<>(errorStrResource));
        mIsScanning.postValue(false);
        mConnectionState.postValue(Constants.DISCONNECTED);
    }

    protected void disposeScanning() {
        Timber.d("Disposing Scanning.");
        if (mScanDisposable != null && !mScanDisposable.isDisposed()) {
            mScanDisposable.dispose();
        }
        mScanDisposable = null;
        mIsScanning.postValue(false);
    }

    protected void disposeConnection() {
        disposeConnection(Constants.DISCONNECTED);
    }

    private void disposeConnectionAndWaitPrepareForReconnect() {
        Timber.d("Started disconnection before reconnecting");
        mDisconnectedEventSubject = SingleSubject.create();
        isReconnecting = true;
        disposeConnection(Constants.RECONNECTING);
    }

    protected void disposeConnection(String newState) {
        Timber.d("Disposing Connection.");
        if (mConnectionDisposable != null && !mConnectionDisposable.isDisposed()) {
            mConnectionDisposable.dispose();
        }
        mConnectionDisposable = null;
        mConnectionState.postValue(newState);
    }

    protected void onConnectionStateChanged(RxBleConnection.RxBleConnectionState rxBleConnectionState) {
        Timber.d("ConnectionState changed. New state: %1$s", rxBleConnectionState.toString());
        if (rxBleConnectionState.equals(RxBleConnection.RxBleConnectionState.DISCONNECTED)) {
            if (isReconnecting) {
                Timber.d("Posting disconnected event to subject");
                mDisconnectedEventSubject.onSuccess(Constants.DISCONNECTED);
            } else {
                if (mConnectionStateDisposable != null) {
                    mConnectionStateDisposable.dispose();
                    mConnectionStateDisposable = null;
                }
                mConnectionState.postValue(Constants.DISCONNECTED);
            }
            isReconnecting = false;
        }
    }

    protected Observable<String> writeAndReadOnNotificationUntilConditionMet(UUID writeTo, UUID readOn,
                                                                             String hexString,
                                                                             boolean isIndication,
                                                                             RxBleConnection rxBleConnection,
                                                                             String condition) {
        Observable<Observable<byte[]>> notifObservable =
                isIndication ?
                        rxBleConnection.setupIndication(readOn) :
                        rxBleConnection.setupNotification(readOn);
        return notifObservable.flatMap(
                (notificationObservable) -> Observable.combineLatest(
                        rxBleConnection.writeCharacteristic(writeTo, hexToBytes(hexString)).toObservable(),
                        notificationObservable.takeUntil(bytes -> {
                            Timber.d("received %s", bytesToHex(bytes));
                            return bytesToHex(bytes).startsWith(condition);
                        })
                                .lastElement().toObservable(),
                        (writtenBytes, responseBytes) -> {
                            Timber.d("Wrote %1$s to %2$s - Resource: %3$s",
                                    bytesToHex(writtenBytes), writeTo.toString(), bytesToHex(responseBytes));
                            return bytesToHex(responseBytes);
                        }
                )
        ).observeOn(Schedulers.io()).doOnError(this::throwException);
    }

    protected Observable<String> writeAndReadOnNotification(UUID writeTo, UUID readOn,
                                                            String hexString,
                                                            boolean isIndication,
                                                            RxBleConnection rxBleConnection) {
        Observable<Observable<byte[]>> notifObservable =
                isIndication ?
                        rxBleConnection.setupIndication(readOn) :
                        rxBleConnection.setupNotification(readOn);
        return notifObservable.flatMap(
                (notificationObservable) -> Observable.zip(
                        rxBleConnection.writeCharacteristic(writeTo, hexToBytes(hexString)).toObservable(),
                        notificationObservable.take(1),
                        (writtenBytes, responseBytes) -> {
                            Timber.d("Wrote %1$s to %2$s - Resource: %3$s",
                                    bytesToHex(writtenBytes), writeTo.toString(), bytesToHex(responseBytes));
                            return bytesToHex(responseBytes);
                        }
                )
        ).observeOn(Schedulers.io()).doOnError(this::throwException);
    }

    protected Single<byte[]> singleToWrite(
            UUID characteristicUuid,
            String hexString,
            RxBleConnection connection) {
        return connection.writeCharacteristic(characteristicUuid, hexToBytes(hexString))
                .observeOn(Schedulers.io())
                .doOnSuccess(bytes -> Timber.d("Bytes written %1$s", bytesToHex(bytes)))
                .doOnError(this::throwException);
    }

    protected Single<byte[]> singleToRead(
            UUID characteristicUuid,
            RxBleConnection connection) {
        return connection.readCharacteristic(characteristicUuid)
                .observeOn(Schedulers.io())
                .doOnSuccess(bytes -> Timber.d("Bytes read %1$s", bytesToHex(bytes)))
                .doOnError(this::throwException);
    }

    public MutableLiveData<Boolean> isScanning() {
        return mIsScanning;
    }

    public MutableLiveData<String> getConnectionState() {
        return mConnectionState;
    }

    public MediatorLiveData<Boolean> isScanningOrConnecting() {
        return mIsScanningOrConnecting;
    }

    public Boolean isConnected() {
        return mConnectionState.getValue().equals(Constants.CONNECTED);
    }

    public MutableLiveData<Event<Integer>> getErrorEvent() {
        return mErrorEvent;
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

    @Nullable
    @Override
    public abstract IBinder onBind(Intent intent);
}
