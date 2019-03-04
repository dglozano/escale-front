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

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
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

    @Inject
    public ChatRepository(ChatMessageDao dao, EscaleRestApi api, AppExecutors executors,
                          UserDao userDao, UserChatJoinDao userChatJoinDao, ChatDao chatDao) {
        this.mAppExecutors = executors;
        this.mChatMessageDao = dao;
        this.mEscaleRestApi = api;
        this.mChatDao = chatDao;
        this.mUserChatJoinDao = userChatJoinDao;
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
        refreshChatsCompletable(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(() -> Timber.d("Success refresh chats"), e -> {
                    Timber.e(e, "Failed refresh chats");
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


    public Completable refreshMessagesCompletable(Long chatId) {
        return mEscaleRestApi.getChatMessages(chatId)
                .flatMapCompletable(messagesApi -> {
                    Timber.d("Retrieved messages for chat with id %s from Api", chatId);
                    messagesApi.stream()
                            .filter(messageDTO -> mChatMessageDao.chatMessageExists(messageDTO.getId()) != 1)
                            .map(msgApi -> new ChatMessage(msgApi, chatId))
                            .forEach(chatMessage -> {
                                Timber.d("Inserting chatMessage from API %s", chatMessage.getId());
                                mChatMessageDao.insertChatMessage(chatMessage);
                            });

                    return Completable.complete();
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
}
