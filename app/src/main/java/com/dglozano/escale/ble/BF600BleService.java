package com.dglozano.escale.ble;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Pair;

import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.di.annotation.ApplicationScope;
import com.dglozano.escale.di.annotation.BluetoothInfo;
import com.dglozano.escale.repository.BodyMeasurementRepository;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.util.Constants;
import com.dglozano.escale.util.RetryWithDelay;
import com.dglozano.escale.util.ui.Event;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.scan.ScanResult;

import java.util.Date;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import dagger.android.AndroidInjection;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.MaybeSubject;
import timber.log.Timber;

import static com.dglozano.escale.ble.CommunicationHelper.PinIndex;
import static com.dglozano.escale.ble.CommunicationHelper.ScaleUserLimitExcedded;
import static com.dglozano.escale.ble.CommunicationHelper.bytesToHex;
import static com.dglozano.escale.ble.CommunicationHelper.decToHex;
import static com.dglozano.escale.ble.CommunicationHelper.generatePIN;
import static com.dglozano.escale.ble.CommunicationHelper.getCurrentTimeHex;
import static com.dglozano.escale.ble.CommunicationHelper.getHexBirthDate;
import static com.dglozano.escale.ble.CommunicationHelper.getNextDbIncrement;
import static com.dglozano.escale.ble.CommunicationHelper.getPhysicalActivity;
import static com.dglozano.escale.ble.CommunicationHelper.getSexHex;
import static com.dglozano.escale.ble.CommunicationHelper.hexToBytes;
import static com.dglozano.escale.ble.CommunicationHelper.hexToDec;
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
    private Disposable mBatteryDisposable;
    private MutableLiveData<Boolean> mIsMeasurementTriggered;
    private MutableLiveData<Integer> mBatteryLevel;
    private MutableLiveData<Event<MaybeSubject<PinIndex>>> mTriggerScaleCredentialsDialog;
    private MutableLiveData<Event<MaybeSubject<PinIndex>>> mTriggerScaleDeleteUserDialog;
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
        mTriggerScaleDeleteUserDialog = new MutableLiveData<>();

        mBatteryLevel = new MutableLiveData<>();
        mBatteryDisposable = null;
    }

    public void scanForBF600Scale() {
        super.scanBleDevices(rxBleClient, this::connectToScaleAndInitialize, mScaleBleNameString);
    }

    private void connectToScaleAndInitialize(ScanResult scanResult) {
        mConnectionDisposable = super.connectToBleDevice(scanResult)
                .flatMapSingle(rxBleConnection -> communicationInitialization(rxBleConnection)
                        .flatMapCompletable(bytesIgnored -> readBatteryAndSetNotify(rxBleConnection))
                        .andThen(hasScaleUsers(rxBleConnection))
                        .flatMap(hasScaleUsers -> {
                            if (hasScaleUsers) {
                                return tryToLoginWithCredentials(rxBleConnection);
                            } else {
                                return createUserAndLogin(rxBleConnection, true);
                            }
                        })
                )
                .doFinally(this::disposeConnection)
                .subscribe(hexResponse -> Timber.d("Final response %1$s", hexResponse),
                        this::throwException);
    }

    private Completable readBatteryAndSetNotify(RxBleConnection rxBleConnection) {
        mBatteryDisposable = singleToRead(Constants.BATTERY_LEVEL, rxBleConnection)
                .flatMapObservable(bytesRead -> {
                    int batteryLevel = hexToDec(bytesToHex(bytesRead));
                    mBatteryLevel.postValue(batteryLevel);
                    Timber.d("Battery level read %s", batteryLevel);
                    return rxBleConnection.setupNotification(Constants.BATTERY_LEVEL);
                })
                .doOnNext(notificationObservable -> {
                    Timber.d("Battery service notification has been set up");
                })
                .flatMap(notificationObservable -> notificationObservable)
                .subscribe(
                        bytesReadNotified -> {
                            int batteryLevel = hexToDec(bytesToHex(bytesReadNotified));
                            mBatteryLevel.postValue(batteryLevel);
                            Timber.d("Battery level notified %s", batteryLevel);
                        },
                        throwable -> {
                            Timber.e(throwable, "Error while reading Battery level");
                        }
                );
        return Completable.complete();
    }

    private Single<Boolean> tryToLoginWithCredentials(RxBleConnection rxBleConnection) {
        Timber.d("Try to login with credentials");
        return getSavedCredentials()
                .switchIfEmpty(Maybe.defer(this::askUserToEnterCredentials))
                .toSingle()
                .flatMap(pinIndex -> loginUserInScale(pinIndex.index(), pinIndex.pin(), rxBleConnection))
                .zipWith(patientRepository.getLoggedPatientSingle(), (couldConnect, patient) ->
                        updateUserDataIfNecessary(patient, rxBleConnection)
                                .toSingleDefault(couldConnect)
                )
                .flatMap(couldConnect -> couldConnect)
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

    private MaybeSubject<PinIndex> askForCredentialsOfScaleUserToDelete() {
        Timber.d("askUserToEnterCredentials");
        MaybeSubject<PinIndex> maybeSubject = MaybeSubject.create();
        mTriggerScaleDeleteUserDialog.postValue(new Event<>(maybeSubject));
        return maybeSubject;
    }

    private Single<Boolean> createUserAndLogin(RxBleConnection rxBleConnection) {
        return createUserAndLogin(rxBleConnection, false);
    }

    private Single<Boolean> createUserAndLogin(RxBleConnection rxBleConnection, boolean isFirstUser) {
        Timber.d("createUserAndLogin");
        return Single.zip(
                patientRepository.getLoggedPatientSingle().subscribeOn(Schedulers.io()),
                createUserInScale(rxBleConnection)
                        .flatMap(pinAndIndex -> {
                            Timber.d("Pin %s Index %s", pinAndIndex.pin(), pinAndIndex.index());
                            return loginUserInScale(pinAndIndex.index(), isFirstUser ? "0201" : pinAndIndex.pin(), rxBleConnection);
                        }),
                Pair::new)
                .flatMap(patientAndStatus -> {
                    Timber.d("Setting scale user data");
                    Patient patient = patientAndStatus.first;
                    Boolean loginStatus = patientAndStatus.second;
                    return updateUserDataIfNecessary(patient, rxBleConnection)
                            .toSingleDefault(loginStatus);
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

    private Completable updateUserDataIfNecessary(Patient patient, RxBleConnection rxBleConnection) {
        if (patient.isHasToUpdateDataInScale()) {
            return writeUserData(patient.getBirthday(),
                    patient.getGender(),
                    patient.getPhysicalActivity(),
                    patient.getHeightInCm(),
                    rxBleConnection)
                    .flatMapCompletable(ignore -> Completable.complete());
        } else {
            return Completable.complete();
        }
    }

    public Single<byte[]> communicationInitialization(RxBleConnection rxBleConnection) {
        mRxBleConnection = rxBleConnection;
        mConnectionState.postValue(Constants.INITIALIZING);
        return rxBleConnection.discoverServices().delay(500, TimeUnit.MILLISECONDS)
                .flatMap(responseIgnored -> singleToWrite(Constants.CURRENT_TIME, getCurrentTimeHex(), rxBleConnection))
                .flatMap(bytes -> singleToWrite(Constants.CUSTOM_FFF1_UNIT_CHARACTERISTIC, Constants.BYTES_SET_KG, rxBleConnection))
                .doOnError(this::throwException);
    }

    public Single<PinIndex> createUserInScale(RxBleConnection rxBleConnection) {
        String PIN = generatePIN();
        String CMD = String.format(Constants.USER_CREATE_CMD, PIN); // Command number to create
        mConnectionState.postValue(Constants.CREATING_USER);
        return writeAndReadOnNotificationUntilConditionMet(Constants.USER_CONTROL_POINT, Constants.USER_CONTROL_POINT,
                CMD, true, rxBleConnection, "2001")
                .take(1)
                .singleOrError()
                .flatMap(hexResponse -> {
                    if (hexResponse.equals(Constants.USER_CREATE_LIMIT_ERROR))
                        return Single.error(new ScaleUserLimitExcedded());
                    String indexHex = lastNBytes(hexResponse, 2);
                    int index = hexToDec(indexHex);
                    if (index > 8) {
                        index = index % 11;
                        indexHex = decToHex(index);
                    }
                    return Single.just(new PinIndex(indexHex, PIN));
                })
                .onErrorResumeNext(error -> {
                    Timber.d("Error on creation");
                    if (error instanceof ScaleUserLimitExcedded) {
                        return askForCredentialsOfScaleUserToDelete()
                                .toSingle()
                                .flatMap(pinIndex -> deleteUserFromScale(rxBleConnection, pinIndex))
                                .flatMap(ignore -> createUserInScale(rxBleConnection))
                                .onErrorResumeNext(e -> {
                                    if (e instanceof NoSuchElementException) {
                                        return Single.error(new ScaleUserLimitExcedded());
                                    } else {
                                        return Single.error(e);
                                    }
                                });
                    } else {
                        return Single.error(error);
                    }
                })
                .observeOn(Schedulers.io())
                .doOnSuccess(pair -> {
                    Timber.d("Index: %1$s , PIN: %2$s", pair.index(), pair.pin());
                })
                .doOnError(this::throwException);
    }

    public Single<Boolean> deleteUserFromScale(RxBleConnection rxBleConnection, PinIndex pinIndex) {
        String DELETE_CMD = String.format(Constants.USER_DELETE_CMD, pinIndex.index());
        String LOGIN_CMD = String.format(Constants.USER_LOGIN_CMD, pinIndex.index(), pinIndex.pin());
        Timber.d("Login User in Scale to delete. Login command: %1$s ", LOGIN_CMD);
        Timber.d("Delete User in Scale command: %1$s ", DELETE_CMD);
        mConnectionState.postValue(Constants.DELETING_USER);
        return writeAndReadOnNotification(Constants.USER_CONTROL_POINT, Constants.USER_CONTROL_POINT,
                LOGIN_CMD, true, rxBleConnection)
                .take(1)
                .flatMapSingle(hexResponse -> {
                    Timber.d("Checking Logging in Scale Response");
                    if (!hexResponse.equals(Constants.USER_LOGIN_SUCCESS)) {
                        return Single.error(new CommunicationHelper.LoginScaleUserFailed());
                    }
                    return Single.just(hexResponse.equals(Constants.USER_LOGIN_SUCCESS));
                })
                .retryWhen(new RetryWithDelay(2, 1000))
                .singleOrError()
                .flatMapObservable(ignore -> writeAndReadOnNotification(Constants.USER_CONTROL_POINT, Constants.USER_CONTROL_POINT,
                        DELETE_CMD, true, rxBleConnection))
                .take(1)
                .flatMapSingle(hexResponse -> {
                    Timber.d("Checking Delete user in Scale Response");
                    if (!hexResponse.equals(Constants.USER_DELETE_SUCCESS)) {
                        return Single.error(new CommunicationHelper.DeleteScaleUserFailed());
                    }
                    return Single.just(hexResponse.equals(Constants.USER_DELETE_SUCCESS));
                })
                .singleOrError()
                .observeOn(Schedulers.io())
                .doOnError(this::throwException)
                .doOnSuccess(couldDelete -> Timber.d("Could delete user in Scale: %1$s", couldDelete));
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
                        Timber.d("Login unsuccessful, deleting credentials");
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(Constants.SCALE_USER_PIN_SHARED_PREF, "");
                        editor.putString(Constants.SCALE_USER_INDEX_SHARED_PREF, "");
                        editor.apply();
                        return Single.error(new CommunicationHelper.LoginScaleUserFailed());
                    } else {
                        Timber.d("Login successful, saving credentials");
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(Constants.SCALE_USER_PIN_SHARED_PREF, PIN);
                        editor.putString(Constants.SCALE_USER_INDEX_SHARED_PREF, userIndex);
                        editor.apply();
                        return Single.just(hexResponse.equals(Constants.USER_LOGIN_SUCCESS));
                    }
                })
                .retryWhen(new RetryWithDelay(2, 2000))
                .singleOrError()
                .observeOn(Schedulers.io())
                .doOnError(this::throwException)
                .doOnSuccess(couldConnect -> Timber.d("Could connect ? %1$s", couldConnect));
    }

    public Single<byte[]> writeUserData(Date dateOfBirth, Patient.Gender gender, int activity, int heightInCm,
                                        RxBleConnection rxBleConnection) {
        Timber.d("Writing user data");
        mConnectionState.postValue(Constants.SETTING_USER_DATA);
        return singleToWrite(Constants.DATE_OF_BIRTH, getHexBirthDate(dateOfBirth), rxBleConnection)
                .flatMap(bytesWritten -> singleToWrite(Constants.GENDER, getSexHex(gender), rxBleConnection))
                .flatMap(bytesWritten -> singleToWrite(Constants.CUSTOM_FFF3_PH_ACTIVITY_CHARACTERISTIC,
                        getPhysicalActivity(activity), rxBleConnection))
                .flatMap(bytesWritten -> singleToWrite(Constants.HEIGHT, decToHex(heightInCm), rxBleConnection))
                .flatMap(bytesWritten -> singleToRead(Constants.DB_CHANGE_INCREMENT, rxBleConnection))
                .flatMap(bytesRead -> singleToWrite(Constants.DB_CHANGE_INCREMENT,
                        getNextDbIncrement(bytesToHex(bytesRead)), rxBleConnection))
                .flatMap(bytesWritten -> patientRepository.setHasToUpdateDataInScale(false, patientRepository.getLoggedPatientId())
                        .map(response -> bytesWritten))
                .observeOn(Schedulers.io())
                .doOnError(this::throwException);
    }

    public Single<Boolean> hasScaleUsers(RxBleConnection rxBleConnection) {
        Timber.d("Checking if scale has users...");
        return writeAndReadOnNotification(Constants.CUSTOM_FFF2_USER_LIST_CHARACTERISTIC, Constants.CUSTOM_FFF2_USER_LIST_CHARACTERISTIC,
                "00", false, rxBleConnection)
                .take(1)
                .flatMapSingle(hexResponse -> Single.just(!hexResponse.equals("02")))
                .singleOrError()
                .observeOn(Schedulers.io())
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
                    .map(bodyMeasurement -> {
                        hasMeasuredDuringThisConnection = true;
                        return bodyMeasurement;
                    })
                    .flatMap(this::saveMeasurementToDb)
                    .flatMapCompletable(id -> {
                        mIsMeasurementTriggered.postValue(false);
                        return patientRepository.getUpdatedForecastFromApi(patientRepository.getLoggedPatientId());
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .doOnDispose(() -> mIsMeasurementTriggered.postValue(false))
                    .doOnSubscribe(d -> mIsMeasurementTriggered.postValue(true))
                    .doOnError(this::throwException)
                    .subscribe(() -> {
                                Timber.d("New body measurement saved to server and locally and updated forecast");
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
            disposeBatteryNotification();
            mConnectionDisposable = super.reconnectToBleDevice()
                    .flatMapSingle(rxBleConnection -> rxBleConnection.discoverServices().delay(600, TimeUnit.MILLISECONDS)
                            .flatMapCompletable(responseIgnored -> readBatteryAndSetNotify(rxBleConnection))
                            .andThen(tryToLoginWithCredentials(rxBleConnection))
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
        bodyMeasurement.setUserId(patientRepository.getLoggedPatientId());
        bodyMeasurement.setManual(false);
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
        if (mMeasurementTriggerDisposable != null && !mMeasurementTriggerDisposable.isDisposed()) {
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

    public MutableLiveData<Event<MaybeSubject<PinIndex>>> showScaleCredentialsDialogToDeleteUserEvent() {
        return mTriggerScaleDeleteUserDialog;
    }

    @Override
    public void disposeConnection() {
        super.disposeConnection();
        hasMeasuredDuringThisConnection = false;
        disposeBatteryNotification();
    }

    private void disposeBatteryNotification() {
        if (mBatteryDisposable != null && !mBatteryDisposable.isDisposed()) {
            mBatteryDisposable.dispose();
            mBatteryDisposable = null;
            mBatteryLevel.postValue(-1);
        }
    }

    public LiveData<Integer> getBatteryLevel() {
        return mBatteryLevel;
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
