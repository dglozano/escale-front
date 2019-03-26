package com.dglozano.escale.ble;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.di.annotation.ApplicationScope;
import com.dglozano.escale.di.annotation.BluetoothInfo;
import com.dglozano.escale.repository.BodyMeasurementRepository;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.util.Constants;
import com.dglozano.escale.util.ui.Event;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.scan.ScanResult;

import java.util.Date;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.MaybeSubject;
import timber.log.Timber;

import static com.dglozano.escale.ble.CommunicationHelper.PinIndex;
import static com.dglozano.escale.ble.CommunicationHelper.ScaleUserLimitExcedded;
import static com.dglozano.escale.ble.CommunicationHelper.bytesToHex;
import static com.dglozano.escale.ble.CommunicationHelper.generatePIN;
import static com.dglozano.escale.ble.CommunicationHelper.getCurrentTimeHex;
import static com.dglozano.escale.ble.CommunicationHelper.getHexBirthDate;
import static com.dglozano.escale.ble.CommunicationHelper.getNextDbIncrement;
import static com.dglozano.escale.ble.CommunicationHelper.getPhysicalActivity;
import static com.dglozano.escale.ble.CommunicationHelper.getSexHex;
import static com.dglozano.escale.ble.CommunicationHelper.hexToBytes;
import static com.dglozano.escale.ble.CommunicationHelper.lastNBytes;
import static com.dglozano.escale.ble.CommunicationHelper.parseFullMeasurementFromHex;

@ApplicationScope
public class BF600BleService extends BaseBleService {

    @Inject
    RxBleClient rxBleClient;
    @Inject
    @BluetoothInfo
    String mScaleBleNameString;
    @Inject
    PatientRepository patientRepository;
    @Inject
    BodyMeasurementRepository bodyMeasurementRepository;
    @Inject
    SharedPreferences sharedPreferences;

    private Disposable mMeasurementTriggerDisposable;
    private MutableLiveData<Boolean> mIsMeasurementTriggered;
    private MutableLiveData<Event<MaybeSubject<PinIndex>>> mTriggerScaleCredentialsDialog;
    private MutableLiveData<Boolean> mIsDoingQuickRefreshOfConnection;
    private boolean hasMeasuredDuringThisConnection = false;

    protected IBinder mBinder;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
        mBinder = new BF600BleService.LocalBinder();

        mIsMeasurementTriggered = new MutableLiveData<>();
        mIsMeasurementTriggered.setValue(false);

        mTriggerScaleCredentialsDialog = new MutableLiveData<>();
    }

    public void scanForBF600Scale() {
        super.scanBleDevices(rxBleClient, this::connectToScaleAndInitialize, mScaleBleNameString);
    }

    private void connectToScaleAndInitialize(ScanResult scanResult) {
        mConnectionDisposable = super.connectToBleDevice(scanResult)
                .flatMapSingle(rxBleConnection -> communicationInitialization(rxBleConnection)
                        .flatMap(bytesIgnored -> hasScaleUsers(rxBleConnection))
                        .flatMap(hasScaleUsers -> {
                            if (hasScaleUsers) {
                                return tryToLoginWithCredentials(rxBleConnection);
                            } else {
                                return createUserAndLogin(rxBleConnection);
                            }
                        })
                )
                .doFinally(this::disposeConnection)
                .subscribe(hexResponse -> Timber.d("Final response %1$s", hexResponse),
                        this::throwException);
    }

    private Single<Boolean> tryToLoginWithCredentials(RxBleConnection rxBleConnection) {
        Timber.d("Try to login with credentials");
        return getSavedCredentials()
                .switchIfEmpty(Maybe.defer(this::askUserToEnterCredentials))
                .toSingle()
                .flatMap(pinIndex -> loginUserInScale(pinIndex.index(), pinIndex.pin(), rxBleConnection))
                .onErrorResumeNext(error -> {
                    if (error instanceof NoSuchElementException) {
                        return createUserAndLogin(rxBleConnection);
                    } else {
                        return Single.error(error);
                    }
                })
                .doOnError(this::throwException)
                .doOnSuccess(couldConnect -> {
                    if (couldConnect) {
                        mConnectionState.postValue(Constants.CONNECTED);
                    } else {
                        mConnectionState.postValue(Constants.DISCONNECTED);
                        Timber.d("Couldn't login to scale");
                    }
                });
    }

    private Maybe<PinIndex> getSavedCredentials() {
        Timber.d("getSavedCredentials");
        String index = sharedPreferences.getString(Constants.SCALE_USER_INDEX_SHARED_PREF, "");
        String pin = sharedPreferences.getString(Constants.SCALE_USER_PIN_SHARED_PREF, "");
        if (!index.isEmpty() && !pin.isEmpty()) {
            Timber.d("Saved Credentials for scale : Index %s - PIN %s", index, pin);
            return Maybe.just(new PinIndex(index, pin));
        } else {
            Timber.d("No Saved Credentials for scale");
            return Maybe.empty();
        }
    }

    private MaybeSubject<PinIndex> askUserToEnterCredentials() {
        Timber.d("askUserToEnterCredentials");
        MaybeSubject<PinIndex> maybeSubject = MaybeSubject.create();
        mTriggerScaleCredentialsDialog.postValue(new Event<>(maybeSubject));
        return maybeSubject;
    }

    private Single<Boolean> createUserAndLogin(RxBleConnection rxBleConnection) {
        Timber.d("createUserAndLogin");
        return Single.zip(
                patientRepository.getLoggedPatientSingle().subscribeOn(Schedulers.io()),
                createUserInScale(rxBleConnection).flatMap(pinAndIndex ->
                        loginUserInScale(pinAndIndex.index(), pinAndIndex.pin(), rxBleConnection)),
                Pair::new)
                .flatMap(patientAndStatus -> {
                    Patient patient = patientAndStatus.first;
                    Boolean loginStatus = patientAndStatus.second;
                    return writeUserData(patient.getBirthday(),
                            patient.getGender(),
                            patient.getPhysicalActivity(),
                            rxBleConnection)
                            .map(bytes -> loginStatus);
                })
                .doOnSuccess(couldConnect -> {
                    if (couldConnect) {
                        mConnectionState.postValue(Constants.CONNECTED);
                    } else {
                        mConnectionState.postValue(Constants.DISCONNECTED);
                        Timber.d("Couldn't login to scale");
                    }
                });
    }

    public Single<byte[]> communicationInitialization(RxBleConnection rxBleConnection) {
        mRxBleConnection = rxBleConnection;
        mConnectionState.postValue(Constants.INITIALIZING);
        return rxBleConnection.discoverServices().delay(600, TimeUnit.MILLISECONDS)
                .flatMap(responseIgnored -> singleToWrite(Constants.CURRENT_TIME, getCurrentTimeHex(), rxBleConnection))
                .flatMap(bytes -> singleToWrite(Constants.CUSTOM_FFF1_UNIT_CHARACTERISTIC, Constants.BYTES_SET_KG, rxBleConnection))
                .flatMap(bytes -> singleToRead(Constants.CUSTOM_FFF5_UNKNOWN_CHARACTERISTIC, rxBleConnection))
                .doOnError(this::throwException);
    }

    public Single<PinIndex> createUserInScale(RxBleConnection rxBleConnection) {
        String PIN = generatePIN();
        String CMD = String.format(Constants.USER_CREATE_CMD, PIN); // Command number to create
        mConnectionState.postValue(Constants.CREATING_USER);
        return writeAndReadOnNotification(Constants.USER_CONTROL_POINT, Constants.USER_CONTROL_POINT,
                CMD, true, rxBleConnection)
                .take(1)
                .flatMapSingle(hexResponse -> {
                    if (hexResponse.equals(Constants.USER_CREATE_LIMIT_ERROR))
                        return Single.error(new ScaleUserLimitExcedded());
                    return Single.just(new PinIndex(lastNBytes(hexResponse, 2), PIN));
                })
                .singleOrError()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(pair -> {
                    Timber.d("Index: %1$s , PIN: %2$s", pair.index(), pair.pin());
                })
                .doOnError(this::throwException);
    }

    public Single<Boolean> loginUserInScale(String userIndex, String PIN) {
        return loginUserInScale(userIndex, PIN, mRxBleConnection);
    }

    public Single<Boolean> loginUserInScale(String userIndex, String PIN, RxBleConnection rxBleConnection) {
        String CMD = String.format(Constants.USER_LOGIN_CMD, userIndex, PIN);
        Timber.d("Logging in Scale user. Login command: %1$s ", CMD);
        mConnectionState.postValue(Constants.LOGGING_IN);
        return writeAndReadOnNotification(Constants.USER_CONTROL_POINT, Constants.USER_CONTROL_POINT,
                CMD, true, rxBleConnection)
                .take(1)
                .flatMapSingle(hexResponse -> {
                    if (!hexResponse.equals(Constants.USER_LOGIN_SUCCESS)) {
                        return Single.error(new CommunicationHelper.LoginScaleUserFailed());
                    }
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Constants.SCALE_USER_PIN_SHARED_PREF, PIN);
                    editor.putString(Constants.SCALE_USER_INDEX_SHARED_PREF, userIndex);
                    editor.apply();
                    return Single.just(hexResponse.equals(Constants.USER_LOGIN_SUCCESS));
                })
                .singleOrError()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(couldConnect -> Timber.d("Logging in Resource: %1$s", couldConnect))
                .doOnError(this::throwException);
    }

    public Single<byte[]> writeUserData(Date dateOfBirth, Patient.Gender gender, int activity,
                                        RxBleConnection rxBleConnection) {
        Timber.d("Writing user data");
        mConnectionState.postValue(Constants.SETTING_USER_DATA);
        return singleToWrite(Constants.DATE_OF_BIRTH, getHexBirthDate(dateOfBirth), rxBleConnection)
                .flatMap(bytesWritten -> singleToWrite(Constants.GENDER, getSexHex(gender), rxBleConnection))
                .flatMap(bytesWritten -> singleToWrite(Constants.CUSTOM_FFF3_PH_ACTIVITY_CHARACTERISTIC,
                        getPhysicalActivity(activity), rxBleConnection))
                .flatMap(bytesWritten -> singleToRead(Constants.DB_CHANGE_INCREMENT, rxBleConnection))
                .flatMap(bytesRead -> singleToWrite(Constants.DB_CHANGE_INCREMENT,
                        getNextDbIncrement(bytesToHex(bytesRead)), rxBleConnection))
                .observeOn(AndroidSchedulers.mainThread())
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
                .doOnSuccess(hexResponse -> Timber.d("hasScaleUsers: %1$s", hexResponse))
                .doOnError(this::throwException);
    }

    private Observable<BodyMeasurement> onWeightMeasurementIndicationReceived(Observable<byte[]> weightInd, Observable<byte[]> bodyInd) {
        return Observable.zip(weightInd.take(1), bodyInd.take(1),
                (weightResponse, bodyResponse) -> {
                    Timber.d("Weight response: %s - Body Response %s",
                            bytesToHex(weightResponse),
                            bytesToHex(bodyResponse));
                    return parseFullMeasurementFromHex(bytesToHex(weightResponse), bytesToHex(bodyResponse));
                });
    }

    public void triggerMeasurement() {
        if (!hasMeasuredDuringThisConnection) {
            mMeasurementTriggerDisposable = weightMeasurementSingle(mRxBleConnection, true)
                    .flatMap(this::saveMeasurementToDb)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnDispose(() -> mIsMeasurementTriggered.postValue(false))
                    .doOnSubscribe(d -> mIsMeasurementTriggered.postValue(true))
                    .doOnError(this::throwException)
                    .subscribe(bodyMeasurement -> {
                                Timber.d("New body measurement saved to server and locally - id %s", bodyMeasurement);
                                mIsMeasurementTriggered.postValue(false);
                                hasMeasuredDuringThisConnection = true;
                                stopMeasurement();
                            }, error -> {
                                Timber.e(error);
                                mIsMeasurementTriggered.postValue(false);
                                stopMeasurement();
                            }
                    );
        } else {
            mConnectionDisposable = super.reconnectToBleDevice()
                    .flatMapSingle(rxBleConnection -> rxBleConnection.discoverServices().delay(600, TimeUnit.MILLISECONDS)
                            .flatMap(responseIgnored -> tryToLoginWithCredentials(rxBleConnection))
                            .flatMap(couldConnect -> {
                                if (couldConnect) {
                                    mConnectionState.postValue(Constants.CONNECTED);
                                } else {
                                    mConnectionState.postValue(Constants.DISCONNECTED);
                                    Timber.d("Couldn't login to scale");
                                }
                                return weightMeasurementSingle(rxBleConnection, true);
                            })
                            .flatMap(this::saveMeasurementToDb)
                            .doOnSuccess(id -> hasMeasuredDuringThisConnection = true))
                    .doFinally(this::disposeConnection)
                    .subscribe(hexResponse -> Timber.d("Final response %1$s", hexResponse),
                            this::throwException);
        }
    }

    private Single<Long> saveMeasurementToDb(BodyMeasurement bodyMeasurement) {
        mIsMeasurementTriggered.postValue(false);
        bodyMeasurement.setUserId(patientRepository.getLoggedPatiendId());
        return bodyMeasurementRepository.addMeasurement(bodyMeasurement);
    }

    private Single<BodyMeasurement> weightMeasurementSingle(RxBleConnection rxBleConnection, boolean withFFF4) {
        mIsMeasurementTriggered.postValue(true);
        Observable<Observable<byte[]>> weightIndication = rxBleConnection.setupIndication(Constants.WEIGHT_MEASUREMENT);
        Observable<Observable<byte[]>> bodyMeasurementIndication = rxBleConnection.setupIndication(Constants.BODY_COMPOSITION_MEASUREMENT);
        Single<byte[]> writeToFFF4 = rxBleConnection.writeCharacteristic(
                Constants.CUSTOM_FFF4_ACTIVATE_SCALE_CHARACTERISTIC,
                hexToBytes("00"));

        return Observable.zip(weightIndication, bodyMeasurementIndication,
                (weightInd, bodyInd) -> {
                    Timber.d("Weight ind %s - BodyInd %s", weightInd, bodyInd);
                    if (withFFF4) {
                        return writeToFFF4.flatMapObservable(response ->
                                onWeightMeasurementIndicationReceived(weightInd, bodyInd));
                    } else {
                        return onWeightMeasurementIndicationReceived(weightInd, bodyInd);
                    }
                })
                .flatMap(unflatten -> unflatten)
                .take(1)
                .singleOrError();
    }

    public void stopMeasurement() {
        if (mMeasurementTriggerDisposable != null) {
            mMeasurementTriggerDisposable.dispose();
            mMeasurementTriggerDisposable = null;
        }
    }

    public LiveData<Boolean> getIsMeasurementTriggered() {
        return mIsMeasurementTriggered;
    }

    public MutableLiveData<Event<MaybeSubject<PinIndex>>> showScaleCredentialsDialogEvent() {
        return mTriggerScaleCredentialsDialog;
    }

    @Override
    public void disposeConnection() {
        super.disposeConnection();
        hasMeasuredDuringThisConnection = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Timber.d("onBind(). Returning binder...");
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public BF600BleService getService() {
            return BF600BleService.this;
        }
    }
}
