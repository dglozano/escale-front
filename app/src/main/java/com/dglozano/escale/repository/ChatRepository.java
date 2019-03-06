package com.dglozano.escale.repository;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;

import com.dglozano.escale.db.dao.ChatDao;
import com.dglozano.escale.db.dao.ChatMessageDao;
import com.dglozano.escale.db.dao.UserChatJoinDao;
import com.dglozano.escale.db.dao.UserDao;
import com.dglozano.escale.db.entity.Chat;
import com.dglozano.escale.db.entity.ChatMessage;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.db.entity.UserChatJoin;
import com.dglozano.escale.di.annotation.ApplicationScope;
import com.dglozano.escale.util.AppExecutors;
import com.dglozano.escale.web.EscaleRestApi;
import com.dglozano.escale.web.dto.SendChatMessageDTO;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@ApplicationScope
public class ChatRepository {

    private ChatMessageDao mChatMessageDao;
    private ChatDao mChatDao;
    private UserChatJoinDao mUserChatJoinDao;
    private EscaleRestApi mEscaleRestApi;
    private AppExecutors mAppExecutors;
    private PatientRepository mPatientRepository;

    @Inject
    public ChatRepository(ChatMessageDao dao, EscaleRestApi api, AppExecutors executors,
                          UserDao userDao, UserChatJoinDao userChatJoinDao, ChatDao chatDao,
                          PatientRepository patientRepository) {
        this.mAppExecutors = executors;
        this.mChatMessageDao = dao;
        this.mEscaleRestApi = api;
        this.mChatDao = chatDao;
        this.mUserChatJoinDao = userChatJoinDao;
        this.mPatientRepository = patientRepository;
    }

    public LiveData<List<Chat>> getAllChatsOfUser(Long userId) {
        refreshChatsOfUser(userId);
        return mUserChatJoinDao.getChatsForUserAsLiveData(userId);
    }

    public LiveData<Long> getChatIdOfPatient(Long loggedPatiendId) {
        refreshChatsOfUser(loggedPatiendId);
        return mUserChatJoinDao.getChatOfLoggedPatientAsLiveData(loggedPatiendId);
    }

    public LiveData<List<ChatMessage>> getMessagesOfChatWithId(Long chatId) {
        Timber.d("Chat id %s", chatId);
        if (chatId != null) {
            refreshMessages(chatId);
        } else {
            chatId = -1L;
        }
        return mChatMessageDao.getAllMessagesOfChatAsLiveData(chatId);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    private void refreshChatsOfUser(final Long userId) {
        refreshChatsOfUserAsMaybe(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe((chatId) -> Timber.d("Success refresh chats. New chat %s", chatId), e -> {
                    if(e instanceof NoSuchElementException){
                        Timber.d("No chats for user yet");
                    } else {
                        Timber.e(e, "Failed refresh chats");
                    }
                });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    private void refreshMessages(final Long chatId) {
        refreshMessagesCompletable(chatId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(() -> Timber.d("Success refresh messages"), e -> {
                    Timber.e(e, "Failed refresh messages");
                });
    }

    public Completable refreshChatsCompletable(Long userId) {
        return mEscaleRestApi.getChatOfUser(userId)
                .flatMapObservable(Observable::fromIterable)
                .filter(chatDTO -> mChatDao.chatExists(chatDTO.getId()) != 1)
                .flatMapSingle(chatDTO -> {
                    return Single.fromCallable(() -> {
                        Chat chat = new Chat(chatDTO.getId());
                        Timber.d("Inserting chat from API %s", chatDTO.getId());
                        mChatDao.insert(chat);
                        return chatDTO;
                    });
                })
                .flatMapCompletable(chatDTO -> {
                    return Completable.fromCallable(() -> {
                        for (Long id : chatDTO.getParticipantsIds()) {
                            Timber.d("Inserting chatjoin %s - %s", id, chatDTO.getId());
                            mUserChatJoinDao.insert(new UserChatJoin(id, chatDTO.getId()));
                        }
                        return Completable.complete();
                    });
                });
    }

    public Maybe<Long> refreshChatsOfUserAsMaybe(Long userId) {
        return mEscaleRestApi.getChatOfUser(userId)
                .flatMapMaybe(list -> {
                    if (list == null || list.isEmpty())
                        return Maybe.empty();
                    else
                        return Maybe.just(list.get(0));
                })
                .filter(chatDTO -> mChatDao.chatExists(chatDTO.getId()) != 1)
                .map(chatDTO -> {
                    Chat chat = new Chat(chatDTO.getId());
                    Timber.d("Inserting chat from API %s", chatDTO.getId());
                    mChatDao.insert(chat);
                    return chatDTO;
                })
                .map(chatDTO -> {
                    for (Long id : chatDTO.getParticipantsIds()) {
                        Timber.d("Inserting chatjoin %s - %s", id, chatDTO.getId());
                        mUserChatJoinDao.insert(new UserChatJoin(id, chatDTO.getId()));
                    }
                    return chatDTO.getId();
                });
    }


    public Completable refreshMessagesCompletable(Long chatId) {
        return refreshMessagesAndCount(chatId)
                .flatMapCompletable(messagesAdded -> {
                    Timber.d("Got %s new messages from API", messagesAdded);
                    return Completable.complete();
                });
    }

    public Single<Integer> refreshMessagesAndCount(Long chatId) {
        return mEscaleRestApi.getChatMessages(chatId)
                .map(messagesApi -> {
                    Timber.d("Retrieved messages for chat with id %s from Api", chatId);
                    List<ChatMessage> newMessagesToAdd = messagesApi.stream()
                            .filter(messageDTO -> mChatMessageDao.chatMessageExists(messageDTO.getId()) != 1)
                            .map(msgApi -> new ChatMessage(msgApi, chatId))
                            .collect(Collectors.toList());
                    newMessagesToAdd.forEach(chatMessage -> {
                        Timber.d("Inserting chatMessage from API %s", chatMessage.getId());
                        mChatMessageDao.insertChatMessage(chatMessage);
                    });
                    return newMessagesToAdd.size();
                });
    }

    public Single<Long> createChat(Long otherUserId) {
        return mEscaleRestApi.createChatForLoggedUser(otherUserId)
                .flatMap(chatDTO -> {
                    Timber.d("Created chat for logged user with other user %s. Chat id %s", otherUserId, chatDTO.getId());
                    Chat chat = new Chat(chatDTO.getId());
                    mChatDao.insert(chat);
                    chatDTO.getParticipantsIds().forEach(participantId -> {
                        mUserChatJoinDao.insert(new UserChatJoin(participantId, chat.getId()));
                    });
                    return Single.just(chat.getId());
                });
    }

    public Completable sendMessage(String message, Patient patient) {
        return mUserChatJoinDao.getChatOfLoggedPatient(patient.getId())
                .flatMap(chatId -> {
                    if (chatId.isPresent()) {
                        return Single.just(chatId.get());
                    } else {
                        return createChat(patient.getDoctorId());
                    }
                }).flatMap(chatId -> mEscaleRestApi.sendChatMessage(chatId,
                        new SendChatMessageDTO(message, Calendar.getInstance().getTime())))
                .flatMapCompletable(msgDTO -> {
                    mChatMessageDao.insertChatMessage(new ChatMessage(msgDTO, msgDTO.getChatId()));
                    return Completable.complete();
                });
    }

    public Completable saveMessageOnReceivedFromDoctor(Long id, Long chatIdInMessage, Long sender_id,
                                                       String msg, String dateString) {
        Long patientId = mPatientRepository.getLoggedPatiendId();
        return mUserChatJoinDao.getChatOfLoggedPatient(patientId)
                .flatMap(chatIdOpt -> {
                    if (chatIdOpt.isPresent()) {
                        return Single.just(chatIdOpt.get());
                    } else {
                        return Single.fromCallable(() -> {
                            Chat chat = new Chat(chatIdInMessage);
                            mChatDao.insert(chat);
                            mUserChatJoinDao.insert(new UserChatJoin(patientId, chatIdInMessage));
                            mUserChatJoinDao.insert(new UserChatJoin(sender_id, chatIdInMessage));
                            return chatIdInMessage;
                        });
                    }
                })
                .flatMapCompletable(chatId -> {
                    if (!chatId.equals(chatIdInMessage)) {
                        throw new Exception(String.format("The chat id received in the message (%s)" +
                                " does not match local chat id (%s) of patient.", chatId, chatIdInMessage));
                    }
                    String format = "yyyy-MM-dd'T'HH:mm:ss.SSS";
                    SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                    Date date = sdf.parse(dateString);
                    mChatMessageDao.insertChatMessage(new ChatMessage(id, chatId, sender_id, msg, date));
                    return Completable.complete();
                });
    }

    public Single<Integer> refreshMessagesAndCountOfPatientWithId(Long patientId) {
        return mUserChatJoinDao.getChatOfLoggedPatient(patientId)
                .map(chatId -> chatId.orElseGet(() -> refreshChatsOfUserAsMaybe(patientId).blockingGet()))
                .flatMap(this::refreshMessagesAndCount);
    }
}
