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
import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.di.annotation.ApplicationScope;
import com.dglozano.escale.di.annotation.BluetoothInfo;
import com.dglozano.escale.util.Constants;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.exceptions.BleScanException;
import com.polidea.rxandroidble2.internal.RxBleLog;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.dglozano.escale.ble.CommunicationHelper.PinIndex;
import static com.dglozano.escale.ble.CommunicationHelper.ScaleUserLimitExcedded;
import static com.dglozano.escale.ble.CommunicationHelper.bytesToHex;
import static com.dglozano.escale.ble.CommunicationHelper.flipBytes;
import static com.dglozano.escale.ble.CommunicationHelper.generatePIN;
import static com.dglozano.escale.ble.CommunicationHelper.getCurrentTimeHex;
import static com.dglozano.escale.ble.CommunicationHelper.hexToBytes;
import static com.dglozano.escale.ble.CommunicationHelper.isSetToKilo;
import static com.dglozano.escale.ble.CommunicationHelper.lastNBytes;
import static com.dglozano.escale.ble.CommunicationHelper.parseDateStringFromHex;
import static com.dglozano.escale.ble.CommunicationHelper.parseWeightMeasurementFromHex;

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
    private MutableLiveData<String> mConnectionState;
    private MutableLiveData<BodyMeasurement> mLastBodyMeasurement;
    private MediatorLiveData<Boolean> mIsScanningOrConnecting;
    private Observable<RxBleConnection> mConnectionObservable;
    private RxBleConnection mRxBleConnection;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
        Timber.d("onCreate.");
        RxBleClient.setLogLevel(RxBleLog.VERBOSE);
        mBinder = new LocalBinder();
        mCompositeDisposable = new CompositeDisposable();
        mIsScanning = new MutableLiveData<>();
        mConnectionState = new MutableLiveData<>();
        mLastBodyMeasurement = new MutableLiveData<>();
        mIsScanning.setValue(false);
        mConnectionState.setValue(Constants.DISCONNECTED);
        mLastBodyMeasurement.setValue(null);
        mIsScanningOrConnecting = new MediatorLiveData<>();
        prepareScanningOrConnectionMediator();
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

    public void scanBleDevices() {
        Timber.d("Start scanning.");
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
        mConnectionState.postValue(Constants.SCANNING);
        mIsScanning.postValue(true);
    }

    private void connectToBleDevice(ScanResult scanResult) {
        mConnectionState.postValue(Constants.BONDING);
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

        mConnectionObservable = prepareObservableForConnection(scaleDevice);

        mConnectionDisposable =
                BondingHelper.bondWithDevice(this, scaleDevice, 10, TimeUnit.SECONDS)
                        .andThen(Completable.fromAction(() -> mConnectionState.postValue(Constants.CONNECTING)))
                        .andThen(mConnectionObservable)
                        .flatMapSingle(rxBleConnection -> communicationInitialization(rxBleConnection)
                                .flatMap(bytesIgnored -> hasScaleUsers(rxBleConnection))
                                .flatMap(hasUsers -> createScaleUserIfNecessaryAndLogin(hasUsers, rxBleConnection))
                                .flatMap(isLoginSuccessful -> triggerWeightMeasurement(rxBleConnection)) // TODO: ADD LOGIC TO PUT CREDENTEIALS
                        )
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io()) // TODO: Check this
                        .doFinally(this::disposeConnection)
                        .subscribe(hexResponse -> Timber.d("Final response %1$s", hexResponse), this::throwException);
    }

    private Observable<RxBleConnection> prepareObservableForConnection(RxBleDevice scaleDevice) {
        return scaleDevice
                .establishConnection(false)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(this::throwException);
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
        mConnectionState.postValue(Constants.DISCONNECTED);
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
        mConnectionState.postValue(Constants.DISCONNECTED);
    }

    private void onConnectionStateChanged(RxBleConnection.RxBleConnectionState rxBleConnectionState) {
        Timber.d("ConnectionState changed. New state: %1$s", rxBleConnectionState.toString());
        if (rxBleConnectionState.equals(RxBleConnection.RxBleConnectionState.DISCONNECTED)) {
            mConnectionState.postValue(Constants.DISCONNECTED);
        }
    }

    private Single<byte[]> singleToWrite(
            UUID characteristicUuid,
            String hexString,
            RxBleConnection connection) {
        return connection.writeCharacteristic(characteristicUuid, hexToBytes(hexString))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(bytes -> Timber.d("Bytes written %1$s", bytesToHex(bytes)))
                .doOnError(this::throwException);
    }

    private Single<byte[]> singleToRead(
            UUID characteristicUuid,
            RxBleConnection connection) {
        return connection.readCharacteristic(characteristicUuid)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(bytes -> Timber.d("Bytes read %1$s", bytesToHex(bytes)))
                .doOnError(this::throwException);
    }

    public Single<byte[]> communicationInitialization(RxBleConnection rxBleConnection) {
        mRxBleConnection = rxBleConnection;
        mConnectionState.postValue(Constants.INITIALIZING);
        return rxBleConnection.discoverServices()
                .flatMap(responseIgnored -> singleToWrite(Constants.CURRENT_TIME, getCurrentTimeHex(), rxBleConnection))
                .flatMap(bytes -> singleToWrite(Constants.CUSTOM_FFF1_UNIT_CHARACTERISTIC, Constants.BYTES_SET_KG, rxBleConnection))
                .flatMap(bytes -> singleToRead(Constants.CURRENT_TIME, rxBleConnection))
                .flatMap(bytes -> {
                    Timber.d("Date from scale is: %1$s", parseDateStringFromHex(bytesToHex(bytes)));
                    return singleToRead(Constants.CUSTOM_FFF1_UNIT_CHARACTERISTIC, rxBleConnection);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess((bytes) -> {
                    Timber.d("Set to kg: %1$s", isSetToKilo(bytesToHex(bytes)));
                    Timber.d("Finished initialization");
                    mConnectionState.postValue(Constants.CONNECTED);
                })
                .doOnError(this::throwException);
    }

    private Single<Boolean> createScaleUserIfNecessaryAndLogin(Boolean hasUsers, RxBleConnection rxBleConnection) {
        Timber.d("HasUsers: %1$s", hasUsers);
        boolean hasCredentials = true; //FIXME
        return hasUsers && hasCredentials ?
                loginUserInScale("00", "0000", rxBleConnection) :
                createUserInScale(rxBleConnection)
                        .flatMap(pinAndIndex -> loginUserInScale(pinAndIndex.index(), pinAndIndex.pin(), rxBleConnection));
    }

    public Single<PinIndex> createUserInScale(RxBleConnection rxBleConnection) {
        String PIN = generatePIN(); // I need to flip these bytes to write it
        String CMD = String.format(Constants.USER_CREATE_CMD, flipBytes(PIN)); // Command number to create
        return writeAndReadOnNotification(Constants.USER_CONTROL_POINT, Constants.USER_CONTROL_POINT,
                CMD, true, rxBleConnection)
                .take(1)
                .flatMapSingle(hexResponse -> {
                    if (hexResponse.equals(Constants.USER_CREATE_LIMIT_ERROR))
                        throw new ScaleUserLimitExcedded();
                    return Single.just(new PinIndex(lastNBytes(hexResponse, 2), PIN));
                })
                .singleOrError()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(pair -> Timber.d("Index: %1$s , PIN: %2$s", pair.index(), pair.pin()))
                .doOnError(this::throwException);
        // Set notification
        // Write byte
        // Convert PIN
        // Wait for notification --> Succeded (return index), maxError, timeout
    }

    public Single<Boolean> loginUserInScale(String userIndex, String PIN) {
        return loginUserInScale(userIndex, PIN, mRxBleConnection);
    }

    public Single<Boolean> loginUserInScale(String userIndex, String PIN, RxBleConnection rxBleConnection) {
        String CMD = String.format(Constants.USER_LOGIN_CMD, userIndex, flipBytes(PIN)); // Command number to create
        Timber.d("Logging in Scale user. Login command: %1$s ", CMD);
        return writeAndReadOnNotification(Constants.USER_CONTROL_POINT, Constants.USER_CONTROL_POINT,
                CMD, true, rxBleConnection)
                .take(1)
                .flatMapSingle(hexResponse -> Single.just(hexResponse.equals(Constants.USER_LOGIN_SUCCESS)))
                .singleOrError()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(hexResponse -> Timber.d("Response: %1$s", hexResponse))
                .doOnError(this::throwException);
    }

    public Single<Boolean> hasScaleUsers(RxBleConnection rxBleConnection) {
        Timber.d("Checking if scale has users...");
        return writeAndReadOnNotification(Constants.CUSTOM_FFF2_USER_LIST_CHARACTERISTIC, Constants.CUSTOM_FFF2_USER_LIST_CHARACTERISTIC,
                "00", false, rxBleConnection)
                .take(1)
                .flatMapSingle(hexResponse -> Single.just(!hexResponse.equals("02")))
                .singleOrError()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(hexResponse -> Timber.d("Response: %1$s", hexResponse))
                .doOnError(this::throwException);
    }

    private Single<BodyMeasurement> triggerWeightMeasurement(RxBleConnection rxBleConnection) {
        return writeAndReadOnNotification(Constants.CUSTOM_FFF4_ACTIVATE_SCALE_CHARACTERISTIC, Constants.WEIGHT_MEASUREMENT,
                "00", true, rxBleConnection)
                .take(1)
                .flatMapSingle(hexResponse -> Single.just(parseWeightMeasurementFromHex(hexResponse)))
                .singleOrError()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(bodyMeasurement -> {
                    Timber.d("Response Weight: %1$s", bodyMeasurement.getWeight());
                    mLastBodyMeasurement.postValue(bodyMeasurement);
                })
                .doOnError(this::throwException);
    }

    private Observable<String> writeAndReadOnNotification(UUID writeTo, UUID readOn,
                                                          String hexString,
                                                          boolean isIndication,
                                                          RxBleConnection rxBleConnection) {
        Observable<Observable<byte[]>> notifObservable =
                isIndication ?
                        rxBleConnection.setupIndication(readOn) :
                        rxBleConnection.setupNotification(readOn);
        return notifObservable.flatMap(
                (notificationObservable) -> Observable.combineLatest(
                        rxBleConnection.writeCharacteristic(writeTo, hexToBytes(hexString)).toObservable(),
                        notificationObservable.take(1),
                        (writtenBytes, responseBytes) -> bytesToHex(responseBytes)
                )
        ).observeOn(AndroidSchedulers.mainThread()).doOnError(this::throwException);
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

    public MutableLiveData<String> getLoadingState() {
        return mConnectionState;
    }

    public MutableLiveData<BodyMeasurement> getBodyMeasurement() {
        return mLastBodyMeasurement;
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

    public Boolean isConnected() {
        return mConnectionState.getValue().equals(Constants.CONNECTED);
    }

    public class LocalBinder extends Binder {
        public BleCommunicationService getService() {
            return BleCommunicationService.this;
        }
    }
}
