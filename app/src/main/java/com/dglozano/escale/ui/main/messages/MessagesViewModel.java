package com.dglozano.escale.ui.main.messages;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.util.LongSparseArray;

import com.dglozano.escale.R;
import com.dglozano.escale.db.entity.Chat;
import com.dglozano.escale.db.entity.ChatMessage;
import com.dglozano.escale.repository.ChatRepository;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.util.ui.Event;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class MessagesViewModel extends ViewModel {

    private PatientRepository mPatientRepository;
    private boolean copyMenuVisible;
    private ChatRepository mChatRepository;
    private final LiveData<List<ChatMessage>> mPatientMessages;
    private final LiveData<List<MessageImpl>> mPatientMessagesImpl;
    private final MutableLiveData<Event<Integer>> mErrorEvent;
    private final MutableLiveData<Boolean> mIsSending;
    private final LiveData<List<Chat>> mChat;
    private final LongSparseArray<AuthorImpl> mChatParticipants;
    private CompositeDisposable disposables;

    @Inject
    public MessagesViewModel(PatientRepository patientRepository, ChatRepository chatRepository) {
        mPatientRepository = patientRepository;
        mChatRepository = chatRepository;
        disposables = new CompositeDisposable();
        mErrorEvent = new MutableLiveData<>();
        mChatParticipants = new LongSparseArray<>();
        setChatParticipants();
        mChat = mChatRepository.getAllChatsOfUser(mPatientRepository.getLoggedPatiendId());
        mPatientMessages = Transformations.switchMap(mChat,
                chats -> mChatRepository.getMessagesOfChatWithId(chats.isEmpty() ? null : chats.get(0).getId()));
        mPatientMessagesImpl = Transformations.map(mPatientMessages, chatMsgs -> chatMsgs.stream()
                .map(msg -> new MessageImpl(msg.getId(),
                        msg.getMessage(),
                        mChatParticipants.get(msg.getUserId()),
                        msg.getSentDate())
                )
                .collect(Collectors.toList()));
        mIsSending = new MutableLiveData<>();
        mIsSending.postValue(false);
    }

    private void setChatParticipants() {
        disposables.add(mPatientRepository.getLoggedPatientSingle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(patient -> {
                    Timber.d("Setting up chat participants for users %s and %s", patient.getId(), patient.getDoctorId());
                    mChatParticipants.put(patient.getId(), new AuthorImpl(patient.getId(), patient.getFirstName()));
                    mChatParticipants.put(patient.getDoctorId(), new AuthorImpl(patient.getDoctorId(), "Doctor"));
                }, Timber::e));
    }

    public LiveData<List<MessageImpl>> getMessagesOfPatientChat() {
        return mPatientMessagesImpl;
    }

    public void sendMessage(String message) {
        disposables.add(mPatientRepository.getLoggedPatientSingle()
                .flatMapCompletable(patient -> mChatRepository.sendMessage(message, patient))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(d -> mIsSending.postValue(true))
                .subscribe(() -> mIsSending.postValue(false),
                        error -> {
                            Timber.e(error, "Error sending message");
                            mIsSending.postValue(false);
                            mErrorEvent.postValue(new Event<>(R.string.error_sending_message));
                        })
        );
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }

    public LiveData<Boolean> observeSendingStatus() {
        return mIsSending;
    }

    public LiveData<Event<Integer>> getErrorEvent() {
        return mErrorEvent;
    }

    public void setCopyMenuVisible(boolean b) {
        copyMenuVisible = b;
    }

    public boolean isCopyMenuVisible() {
        return copyMenuVisible;
    }
}