package com.dglozano.escale.ui.main.messages;

import android.util.LongSparseArray;

import com.dglozano.escale.R;
import com.dglozano.escale.db.entity.Chat;
import com.dglozano.escale.db.entity.ChatMessage;
import com.dglozano.escale.repository.ChatRepository;
import com.dglozano.escale.repository.DoctorRepository;
import com.dglozano.escale.repository.PatientRepository;
import com.dglozano.escale.util.ui.Event;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class MessagesViewModel extends ViewModel {

    private PatientRepository mPatientRepository;
    private DoctorRepository mDoctorRepository;
    private boolean copyMenuVisible;
    private ChatRepository mChatRepository;
    private final LiveData<List<ChatMessage>> mMessages;
    private final LiveData<List<MessageImpl>> mMessagesImpl;
    private final MutableLiveData<Event<Integer>> mErrorEvent;
    private final MutableLiveData<Boolean> mIsSending;
    private final LiveData<List<Chat>> mChat;
    private final LongSparseArray<AuthorImpl> mChatParticipants;
    private CompositeDisposable disposables;

    @Inject
    public MessagesViewModel(PatientRepository patientRepository, DoctorRepository doctorRepository, ChatRepository chatRepository) {
        mPatientRepository = patientRepository;
        mDoctorRepository = doctorRepository;
        mChatRepository = chatRepository;
        disposables = new CompositeDisposable();
        mErrorEvent = new MutableLiveData<>();
        mChatParticipants = new LongSparseArray<>();
        setChatParticipants();
        mChat = mChatRepository.getAllChatsOfUser(mPatientRepository.getLoggedPatientId());
        mMessages = Transformations.switchMap(mChat,
                chats -> mChatRepository.getMessagesOfChatWithId(chats.isEmpty() ? null : chats.get(0).getId()));
        mMessagesImpl = Transformations.map(mMessages, chatMsgs -> chatMsgs.stream()
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
        return mMessagesImpl;
    }

    public void sendMessageAsPatient(String message) {
        disposables.add(mPatientRepository.getLoggedPatientSingle()
                .flatMapCompletable(patient -> mChatRepository.sendMessageAsPatient(message, patient))
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

    public void sendMessageAsDoctor(String message) {
        disposables.add(mDoctorRepository.getLoggedDoctorSingle()
                .flatMapCompletable(doctorOptional -> {
                    if (!doctorOptional.isPresent()) {
                        return Completable.error(new Throwable("No doctor with that id"));
                    } else {
                        return mChatRepository.sendMessageAsDoctor(message, doctorOptional.get());
                    }
                })
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