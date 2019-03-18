package com.dglozano.escale.web.services;

import android.content.SharedPreferences;

import com.dglozano.escale.repository.ChatRepository;
import com.dglozano.escale.repository.DietRepository;
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
    PatientRepository patientRepository;

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
            } else if(type.equals("delete_diet")) {
                handleDeleteDietNotification(remoteMessage);
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
                .saveDietOnNotified(patientRepository.getLoggedPatiendId(), uuid, fileName, startDate, size)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(() -> {
                            Timber.d("Received diet from firebase and saved successfully");
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(Constants.HAS_NEW_UNREAD_DIET, true);
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
        disposables.add(chatRepository
                .saveMessageOnReceivedFromDoctor(id, chat_id, sender_id, msg, date)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(() -> {
                            Timber.d("Received message from firebase and saved successfully");
                            Integer current = sharedPreferences.getInt(Constants.UNREAD_MESSAGES_SHARED_PREF, 0);
                            Timber.d("Current amount of unread messages %s", current);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt(Constants.UNREAD_MESSAGES_SHARED_PREF, current + 1);
                            editor.apply();
                        },
                        Timber::e)
        );
    }
}
