package com.dglozano.escale.repository;

import android.annotation.SuppressLint;

import com.dglozano.escale.db.dao.ChatDao;
import com.dglozano.escale.db.dao.ChatMessageDao;
import com.dglozano.escale.db.dao.UserChatJoinDao;
import com.dglozano.escale.db.dao.UserChatMsgSeenJoinDao;
import com.dglozano.escale.db.entity.Chat;
import com.dglozano.escale.db.entity.ChatMessage;
import com.dglozano.escale.db.entity.Doctor;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.db.entity.UserChatJoin;
import com.dglozano.escale.db.entity.UserChatMsgSeenJoin;
import com.dglozano.escale.di.annotation.ApplicationScope;
import com.dglozano.escale.web.EscaleRestApi;
import com.dglozano.escale.web.dto.SendChatMessageDTO;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
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
    private UserChatMsgSeenJoinDao mUserChatMsgSeenJoinDao;
    private EscaleRestApi mEscaleRestApi;
    private PatientRepository mPatientRepository;
    private DoctorRepository mDoctorRepository;

    @Inject
    public ChatRepository(ChatMessageDao dao, EscaleRestApi api, UserChatJoinDao userChatJoinDao,
                          ChatDao chatDao, DoctorRepository doctorRepository,
                          UserChatMsgSeenJoinDao userChatMsgSeenJoinDao,
                          PatientRepository patientRepository) {
        this.mChatMessageDao = dao;
        this.mEscaleRestApi = api;
        this.mChatDao = chatDao;
        this.mUserChatMsgSeenJoinDao = userChatMsgSeenJoinDao;
        this.mUserChatJoinDao = userChatJoinDao;
        this.mPatientRepository = patientRepository;
        this.mDoctorRepository = doctorRepository;
    }

    public LiveData<List<Chat>> getAllChatsOfUser(Long userId) {
        refreshChatsOfUser(userId);
        return mUserChatJoinDao.getChatsForUserAsLiveData(userId);
    }

    public LiveData<Long> getChatIdOfPatient(Long loggedPatiendId) {
        refreshChatsOfUser(loggedPatiendId);
        return mUserChatJoinDao.getChatOfPatientAsLiveData(loggedPatiendId);
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
                    if (e instanceof NoSuchElementException) {
                        Timber.d("No chats for user yet");
                    } else {
                        Timber.e(e, "Failed refresh chats");
                    }
                });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    private void refreshMessages(final Long chatId) {
        refreshMessagesOfChat(chatId)
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
                        mChatDao.upsert(chat);
                        return chatDTO;
                    });
                })
                .flatMapCompletable(chatDTO -> {
                    return Completable.fromCallable(() -> {
                        for (Long id : chatDTO.getParticipantsIds()) {
                            Timber.d("Inserting chatjoin %s - %s", id, chatDTO.getId());
                            mUserChatJoinDao.upsert(new UserChatJoin(id, chatDTO.getId()));
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
                .map(chatDTO -> {
                    Chat chat = new Chat(chatDTO.getId());
                    Timber.d("Upserting chat from API %s", chatDTO.getId());
                    mChatDao.upsert(chat);
                    return chatDTO;
                })
                .map(chatDTO -> {
                    for (Long id : chatDTO.getParticipantsIds()) {
                        Timber.d("Upserting chatjoin %s - %s", id, chatDTO.getId());
                        mUserChatJoinDao.upsert(new UserChatJoin(id, chatDTO.getId()));
                    }
                    return chatDTO.getId();
                });
    }


    public Completable refreshMessagesOfChat(Long chatId) {
        return mEscaleRestApi.getChatMessages(chatId)
                .flatMapCompletable(messagesApi -> {
                    Timber.d("Retrieved messages for chat with id %s from Api", chatId);

                    messagesApi.forEach(msgApi -> {
                        ChatMessage chatMessage = new ChatMessage(msgApi, chatId);
                        Timber.d("Upserting chatMessage from API %s", chatMessage.getId());
                        mChatMessageDao.upsert(chatMessage);
                        msgApi.getSeenBy().forEach(participantId ->
                                mUserChatMsgSeenJoinDao.upsert(new UserChatMsgSeenJoin(participantId, chatMessage.getId())));
                    });

                    return Completable.complete();
                });
    }

    public LiveData<Integer> getUnreadMessagesOfUserInChatAsLiveData(Long userId, Long chatId) {
        return mUserChatMsgSeenJoinDao.getAmountOfUnseenChatMessagesForUserInChat(userId, chatId);
    }

    public Single<Long> createChat(Long otherUserId) {
        return mEscaleRestApi.createChatForLoggedUser(otherUserId)
                .flatMap(chatDTO -> {
                    Timber.d("Created chat for logged user with other user %s. Chat id %s", otherUserId, chatDTO.getId());
                    Chat chat = new Chat(chatDTO.getId());
                    mChatDao.upsert(chat);
                    chatDTO.getParticipantsIds().forEach(participantId -> {
                        mUserChatJoinDao.upsert(new UserChatJoin(participantId, chat.getId()));
                    });
                    return Single.just(chat.getId());
                });
    }

    public Completable sendMessageAsPatient(String message, Patient patient) {
        return mUserChatJoinDao.getChatOfPatientAsOptional(patient.getId())
                .flatMap(chatId -> {
                    if (chatId.isPresent()) {
                        return Single.just(chatId.get());
                    } else {
                        return createChat(patient.getDoctorId());
                    }
                }).flatMap(chatId -> mEscaleRestApi.sendChatMessage(chatId,
                        new SendChatMessageDTO(message, Calendar.getInstance().getTime())))
                .flatMapCompletable(msgDTO -> {
                    mChatMessageDao.upsert(new ChatMessage(msgDTO, msgDTO.getChatId()));
                    return Completable.complete();
                });
    }

    public Completable sendMessageAsDoctor(String message, Doctor doctor) {
        return mUserChatJoinDao.getChatOfPatientAsOptional(mPatientRepository.getLoggedPatientId())
                .flatMap(chatId -> {
                    if (chatId.isPresent()) {
                        return Single.just(chatId.get());
                    } else {
                        return createChat(mPatientRepository.getLoggedPatientId());
                    }
                }).flatMap(chatId -> mEscaleRestApi.sendChatMessage(chatId,
                        new SendChatMessageDTO(message, Calendar.getInstance().getTime())))
                .flatMapCompletable(msgDTO -> {
                    mChatMessageDao.upsert(new ChatMessage(msgDTO, msgDTO.getChatId()));
                    return Completable.complete();
                });
    }

    private Completable saveMessageOnReceivedFromDoctor(Long id, Long chatIdInMessage, Long doctor_id,
                                                        String msg, String dateString) {
        Long patientId = mPatientRepository.getLoggedPatientId();
        return mUserChatJoinDao.getChatOfPatientAsOptional(patientId)
                .flatMap(chatIdOpt -> {
                    if (chatIdOpt.isPresent()) {
                        return Single.just(chatIdOpt.get());
                    } else {
                        return Single.fromCallable(() -> {
                            Chat chat = new Chat(chatIdInMessage);
                            mChatDao.upsert(chat);
                            mUserChatJoinDao.upsert(new UserChatJoin(patientId, chatIdInMessage));
                            mUserChatJoinDao.upsert(new UserChatJoin(doctor_id, chatIdInMessage));
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
                    mChatMessageDao.upsert(new ChatMessage(id, chatId, doctor_id, msg, date));
                    mUserChatMsgSeenJoinDao.upsert(new UserChatMsgSeenJoin(doctor_id, id));
                    return Completable.complete();
                });
    }

    private Completable saveMessageOnReceivedFromPatient(Long id, Long chatIdInMessage, Long patient_id,
                                                         String msg, String dateString) {
        Long doctorId = mDoctorRepository.getLoggedDoctorId();
        return mUserChatJoinDao.getChatOfPatientAsOptional(patient_id)
                .flatMap(chatIdOpt -> {
                    if (chatIdOpt.isPresent()) {
                        return Single.just(chatIdOpt.get());
                    } else {
                        return Single.fromCallable(() -> {
                            Chat chat = new Chat(chatIdInMessage);
                            mChatDao.upsert(chat);
                            mUserChatJoinDao.upsert(new UserChatJoin(patient_id, chatIdInMessage));
                            mUserChatJoinDao.upsert(new UserChatJoin(doctorId, chatIdInMessage));
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
                    mChatMessageDao.upsert(new ChatMessage(id, chatId, patient_id, msg, date));
                    mUserChatMsgSeenJoinDao.upsert(new UserChatMsgSeenJoin(patient_id, id));
                    return Completable.complete();
                });
    }

    public Completable refreshMessagesOfPatientWithId(Long patientId) {
        return mUserChatJoinDao.getChatOfPatientAsMaybe(patientId)
                .switchIfEmpty(refreshChatsOfUserAsMaybe(patientId))
                .toSingle()
                .flatMapCompletable(this::refreshMessagesOfChat);
    }

    public Completable markMessagesAsReadForPatient() {
        return mUserChatJoinDao.getChatOfPatientAsMaybe(mPatientRepository.getLoggedPatientId())
                .switchIfEmpty(refreshChatsOfUserAsMaybe(mPatientRepository.getLoggedPatientId()))
                .toSingle()
                .flatMapCompletable(chatId -> mEscaleRestApi
                        .markSeenByUser(chatId, mPatientRepository.getLoggedPatientId())
                        .andThen(Completable.fromAction(() -> {
                            mChatMessageDao.getAllMessagesOfChat(chatId).stream()
                                    .map(chatMessage -> new UserChatMsgSeenJoin(mPatientRepository.getLoggedPatientId(), chatMessage.getId()))
                                    .forEach(userChatMsgSeenJoin -> mUserChatMsgSeenJoinDao.upsert(userChatMsgSeenJoin));
                        }))
                );
    }

    // TODO: DO NOT UPDATE ALL MESSAGES EVERYTIME.
    public Completable markMessagesAsReadForDoctor() {
        return mUserChatJoinDao.getChatOfPatientAsMaybe(mPatientRepository.getLoggedPatientId())
                .switchIfEmpty(refreshChatsOfUserAsMaybe(mPatientRepository.getLoggedPatientId()))
                .toSingle()
                .flatMapCompletable(chatId -> mEscaleRestApi
                        .markSeenByUser(chatId, mDoctorRepository.getLoggedDoctorId())
                        .andThen(Completable.fromAction(() -> {
                            mChatMessageDao.getAllMessagesOfChat(chatId).stream()
                                    .peek(chatMessage -> mChatMessageDao.upsert(chatMessage))
                                    .map(chatMessage -> new UserChatMsgSeenJoin(mDoctorRepository.getLoggedDoctorId(), chatMessage.getId()))
                                    .forEach(userChatMsgSeenJoin -> mUserChatMsgSeenJoinDao.upsert(userChatMsgSeenJoin));
                        }))
                );
    }

    public Completable saveMessageOnReceived(Long id, Long chat_id, Long sender_id, String msg, String date) {
        if (mDoctorRepository.getLoggedDoctorId() != -1L && mDoctorRepository.getLoggedDoctorId() == sender_id.longValue()) {
            return saveMessageOnReceivedFromPatient(id, chat_id, sender_id, msg, date);
        } else {
            return saveMessageOnReceivedFromDoctor(id, chat_id, sender_id, msg, date);
        }

    }
}
