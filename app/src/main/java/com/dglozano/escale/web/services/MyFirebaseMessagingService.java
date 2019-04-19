package com.dglozano.escale.web.services;

import android.content.SharedPreferences;

import com.dglozano.escale.db.entity.Alert;
import com.dglozano.escale.repository.AlertRepository;
import com.dglozano.escale.repository.ChatRepository;
import com.dglozano.escale.repository.DietRepository;
import com.dglozano.escale.repository.DoctorRepository;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.util.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Inject
    SharedPreferences sharedPreferences;
    @Inject
    ChatRepository chatRepository;
    @Inject
    DietRepository dietRepository;
    @Inject
    DoctorRepository doctorRepository;
    @Inject
    PatientRepository patientRepository;
    @Inject
    AlertRepository alertRepository;

    private CompositeDisposable disposables;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
        disposables = new CompositeDisposable();
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
        super.onDestroy();
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Timber.d("Refreshed Firebase token: %s", token);

        // Saves the token for this device to SharedPreferences.
        // Then, whenever an user logins to the app, it will be triggered when this
        // value changes and will send it to the server, associating it to the logged user.
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.FIREBASE_TOKEN_SHARED_PREF, token);
        editor.putBoolean(Constants.IS_FIREBASE_TOKEN_SENT_SHARED_PREF, false);
        editor.apply();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Timber.d("Message data payload: %s", remoteMessage.getData());
            String type = remoteMessage.getData().get("type");
            if (type.equals("new_message")) {
                handleNewMessageNotification(remoteMessage);
            } else if (type.equals("new_diet")) {
                handleNewDietNotification(remoteMessage);
            } else if (type.equals("delete_diet")) {
                handleDeleteDietNotification(remoteMessage);
            } else if (type.equals("new_goal")) {
                handleNewGoalNotification(remoteMessage);
            } else if (type.equals("new_alert")) {
                handleNewAlertNotification(remoteMessage);
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Timber.d("Message Notification Body: %s", remoteMessage.getNotification().getBody());
        }
    }

    private void handleDeleteDietNotification(RemoteMessage remoteMessage) {
        String uuid = remoteMessage.getData().get("uuid");
        disposables.add(dietRepository
                .deleteDietByUuid(uuid)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(() -> Timber.d("Received delete diet command from firebase and removed successfully"),
                        Timber::e)
        );
    }

    private void handleNewDietNotification(RemoteMessage remoteMessage) {
        String uuid = remoteMessage.getData().get("uuid");
        String fileName = remoteMessage.getData().get("file_name");
        String fileType = remoteMessage.getData().get("file_type");
        String startDate = remoteMessage.getData().get("date");
        Long size = Long.parseLong(remoteMessage.getData().get("size"));
        disposables.add(dietRepository
                .saveDietOnNotified(patientRepository.getLoggedPatientId(), uuid, fileName, startDate, size)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(() -> {
                            Timber.d("Received diet from firebase and saved successfully");
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(Constants.HAS_NEW_UNREAD_DIET_SHARED_PREF, true);
                            editor.apply();
                        },
                        Timber::e)
        );
    }

    private void handleNewMessageNotification(RemoteMessage remoteMessage) {
        Long id = Long.parseLong(remoteMessage.getData().get("id"));
        Long chat_id = Long.parseLong(remoteMessage.getData().get("chat_id"));
        String msg = remoteMessage.getData().get("msg");
        Long sender_id = Long.parseLong(remoteMessage.getData().get("sender_id"));
        String date = remoteMessage.getData().get("date");
        disposables.add(doctorRepository.addMessageToPatientInfo(sender_id)
                .andThen(chatRepository.saveMessageOnReceived(id, chat_id, sender_id, msg, date))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(() -> {
                            Timber.d("Received message from firebase and saved successfully");
                        },
                        Timber::e)
        );
    }

    private void handleNewAlertNotification(RemoteMessage remoteMessage) {
        Long id = Long.parseLong(remoteMessage.getData().get("id"));
        Long patient_id = Long.parseLong(remoteMessage.getData().get("patient_id"));
        Long doctor_id = Long.parseLong(remoteMessage.getData().get("doctor_id"));
        Integer alert_type = Integer.parseInt(remoteMessage.getData().get("alert_type"));
        String alert_msg = remoteMessage.getData().get("alert_msg");
        String date = remoteMessage.getData().get("date");
        disposables.add(doctorRepository.addAlertToPatientInfo(patient_id, doctor_id)
                .andThen(alertRepository.upsertAlert(id, patient_id, doctor_id, Alert.intToAlertType(alert_type), alert_msg, date))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(() -> {
                            Timber.d("Received message from firebase and saved successfully");
                        },
                        Timber::e)
        );
    }

    private void handleNewGoalNotification(RemoteMessage remoteMessage) {
        Long id = Long.parseLong(remoteMessage.getData().get("id"));
        Float weightInKg = Float.parseFloat(remoteMessage.getData().get("goal_weight_kg"));
        String startDate = remoteMessage.getData().get("start_date");
        String dueDate = remoteMessage.getData().get("due_date");
        Boolean isAccomplished = Boolean.parseBoolean(remoteMessage.getData().get("accomplished"));
        disposables.add(patientRepository
                .saveNewGoalOnNotified(patientRepository.getLoggedPatientId(), weightInKg, dueDate, startDate)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(() -> Timber.d("Received new goal from firebase and saved successfully"),
                        Timber::e)
        );
    }
}
