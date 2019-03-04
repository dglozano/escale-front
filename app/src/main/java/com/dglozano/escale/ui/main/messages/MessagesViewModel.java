package com.dglozano.escale.ui.main.messages;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.util.LongSparseArray;

import com.dglozano.escale.db.entity.Chat;
import com.dglozano.escale.db.entity.ChatMessage;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.repository.ChatRepository;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.ui.Event;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class MessagesViewModel extends ViewModel {

    private PatientRepository mPatientRepository;
    private ChatRepository mChatRepository;
    private final LiveData<List<ChatMessage>> mPatientMessages;
    private final LiveData<List<MessageImpl>> mPatientMessagesImpl;
    private final MutableLiveData<Event<Integer>> mErrorEvent;
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
    }

    private void setChatParticipants() {
        Patient patient = mPatientRepository.getLoggedPatient().getValue();
        Long doctorId = patient.getDoctorId();
        mChatParticipants.put(patient.getId(), new AuthorImpl(patient.getId(), patient.getFirstName()));
        mChatParticipants.put(doctorId, new AuthorImpl(doctorId, "Doctor"));
    }

    public LiveData<List<MessageImpl>> getMessagesOfPatientChat() {
        return mPatientMessagesImpl;
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }
}