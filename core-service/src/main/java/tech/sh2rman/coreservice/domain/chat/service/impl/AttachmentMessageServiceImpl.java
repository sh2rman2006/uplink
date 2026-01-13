package tech.sh2rman.coreservice.domain.chat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.sh2rman.coreservice.domain.chat.dto.req.CreateFilesMessageRequest;
import tech.sh2rman.coreservice.domain.chat.dto.req.CreateMediaMessageRequest;
import tech.sh2rman.coreservice.domain.chat.dto.req.CreateVoiceMessageRequest;
import tech.sh2rman.coreservice.domain.chat.dto.res.MessageDto;
import tech.sh2rman.coreservice.domain.chat.entity.Attachment;
import tech.sh2rman.coreservice.domain.chat.entity.Chat;
import tech.sh2rman.coreservice.domain.chat.entity.ChatParticipant;
import tech.sh2rman.coreservice.domain.chat.entity.Message;
import tech.sh2rman.coreservice.domain.chat.exception.MessageBadRequestException;
import tech.sh2rman.coreservice.domain.chat.mapper.MessageMapper;
import tech.sh2rman.coreservice.domain.chat.model.AttachmentType;
import tech.sh2rman.coreservice.domain.chat.model.MessageStatus;
import tech.sh2rman.coreservice.domain.chat.model.MessageType;
import tech.sh2rman.coreservice.domain.chat.repository.AttachmentRepository;
import tech.sh2rman.coreservice.domain.chat.repository.ChatRepository;
import tech.sh2rman.coreservice.domain.chat.repository.MessageRepository;
import tech.sh2rman.coreservice.domain.chat.service.AttachmentMessageService;
import tech.sh2rman.coreservice.domain.chat.service.ChatAccessService;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;
import tech.sh2rman.coreservice.integration.minio.MinioStorageService;
import tech.sh2rman.coreservice.integration.minio.model.UploadedObject;
import tech.sh2rman.coreservice.websocket.dto.WsEvent;
import tech.sh2rman.coreservice.websocket.dto.WsEventType;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentMessageServiceImpl implements AttachmentMessageService {

    private final ChatAccessService access;

    private final MessageRepository messageRepository;
    private final AttachmentRepository attachmentRepository;
    private final ChatRepository chatRepository;

    private final MinioStorageService storage;

    private final MessageMapper messageMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public Message sendMedia(UUID chatId, UUID userId, CreateMediaMessageRequest req) {
        if (req == null || req.files() == null || req.files().isEmpty()) {
            throw new MessageBadRequestException("files are required");
        }

        Chat chat = access.requireChat(chatId);
        ChatParticipant me = access.requireParticipant(chatId, userId);
        UserProfileEntity sender = access.requireUser(userId);

        access.assertCanSend(chat, me);
        access.assertCanSendMedia(chat, me);

        OffsetDateTime now = OffsetDateTime.now();

        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setType(MessageType.IMAGE);
        message.setText(trimToNull(req.text()));
        message.setIsForwarded(false);
        message.setStatus(MessageStatus.SENT);
        message.setEditCount(0);
        message.setCreatedAt(now);
        message.setUpdatedAt(now);

        if (req.replyToMessageId() != null) {
            message.setReplyToMessage(access.requireMessage(chatId, req.replyToMessageId()));
        }

        Message saved = messageRepository.save(message);

        boolean hasVideo = false;
        List<Attachment> created = new ArrayList<>();

        for (MultipartFile f : req.files()) {
            if (f == null || f.isEmpty()) continue;

            String ct = f.getContentType();

            if (ct != null && ct.toLowerCase().startsWith("video/")) {
                String objectKey = storage.putAttachmentVideo(saved.getId(), chatId, f);
                created.add(buildAttachment(saved, AttachmentType.VIDEO, f, objectKey, now));
                hasVideo = true;
            } else {
                String objectKey = storage.putAttachmentImage(saved.getId(), chatId, f);
                created.add(buildAttachment(saved, AttachmentType.IMAGE, f, objectKey, now));
            }
        }

        if (created.isEmpty()) {
            throw new MessageBadRequestException("no valid files to upload");
        }

        attachmentRepository.saveAll(created);

        if (hasVideo) {
            saved.setType(MessageType.VIDEO);
            saved.setUpdatedAt(now);
            saved = messageRepository.save(saved);
        }

        updateChatLast(chat, saved, now);
        publishCreated(chatId, saved);

        if (saved.getAttachments() != null) {
            saved.getAttachments().addAll(created);
        }

        return saved;
    }

    @Override
    @Transactional
    public Message sendFiles(UUID chatId, UUID userId, CreateFilesMessageRequest req) {
        if (req == null || req.files() == null || req.files().isEmpty()) {
            throw new MessageBadRequestException("files are required");
        }

        Chat chat = access.requireChat(chatId);
        ChatParticipant me = access.requireParticipant(chatId, userId);
        UserProfileEntity sender = access.requireUser(userId);

        access.assertCanSend(chat, me);
        access.assertCanSendMedia(chat, me);

        OffsetDateTime now = OffsetDateTime.now();

        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setType(MessageType.FILE);
        message.setText(trimToNull(req.text()));
        message.setIsForwarded(false);
        message.setStatus(MessageStatus.SENT);
        message.setEditCount(0);
        message.setCreatedAt(now);
        message.setUpdatedAt(now);

        if (req.replyToMessageId() != null) {
            message.setReplyToMessage(access.requireMessage(chatId, req.replyToMessageId()));
        }

        Message saved = messageRepository.save(message);

        List<Attachment> created = new ArrayList<>();

        for (MultipartFile f : req.files()) {
            if (f == null || f.isEmpty()) continue;

            UploadedObject up = storage.putAttachmentAnyTyped(saved.getId(), chatId, f);
            created.add(buildAttachment(saved, up.attachmentType(), f, up.objectKey(), now));
        }

        if (created.isEmpty()) {
            throw new MessageBadRequestException("no valid files to upload");
        }

        attachmentRepository.saveAll(created);

        updateChatLast(chat, saved, now);
        publishCreated(chatId, saved);

        if (saved.getAttachments() != null) {
            saved.getAttachments().addAll(created);
        }

        return saved;
    }

    @Override
    @Transactional
    public Message sendVoice(UUID chatId, UUID userId, CreateVoiceMessageRequest req) {
        if (req == null || req.file() == null || req.file().isEmpty()) {
            throw new MessageBadRequestException("file is required");
        }

        Chat chat = access.requireChat(chatId);
        ChatParticipant me = access.requireParticipant(chatId, userId);
        UserProfileEntity sender = access.requireUser(userId);

        access.assertCanSend(chat, me);
        access.assertCanSendMedia(chat, me);

        OffsetDateTime now = OffsetDateTime.now();

        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setType(MessageType.VOICE);
        message.setText(null);
        message.setIsForwarded(false);
        message.setStatus(MessageStatus.SENT);
        message.setEditCount(0);
        message.setCreatedAt(now);
        message.setUpdatedAt(now);

        if (req.replyToMessageId() != null) {
            message.setReplyToMessage(access.requireMessage(chatId, req.replyToMessageId()));
        }

        Message saved = messageRepository.save(message);

        String objectKey = storage.putAttachmentAudio(saved.getId(), chatId, req.file());

        Attachment a = buildAttachment(saved, AttachmentType.VOICE_MESSAGE, req.file(), objectKey, now);
        attachmentRepository.save(a);

        updateChatLast(chat, saved, now);
        publishCreated(chatId, saved);

        if (saved.getAttachments() != null) {
            saved.getAttachments().add(a);
        }

        return saved;
    }

    private Attachment buildAttachment(
            Message message,
            AttachmentType type,
            MultipartFile file,
            String objectKey,
            OffsetDateTime now
    ) {
        Attachment a = new Attachment();

        a.setMessage(message);
        a.setType(type);

        a.setMimeType(file.getContentType());
        a.setFileName(trimToNull(file.getOriginalFilename()));
        a.setFileSize(file.getSize());
        a.setObjectKey(objectKey);
        a.setBucketName(storage.getBucketNameSafe());
        a.setHasThumbnail(false);

        a.setUploadStatus("UPLOADED");
        a.setUploadProgress(100);
        a.setUploadedAt(now);

        a.setCreatedAt(now);
        a.setUpdatedAt(now);

        return a;
    }


    private void updateChatLast(Chat chat, Message saved, OffsetDateTime now) {
        chat.setLastMessageAt(now);
        chat.setLastMessageId(saved.getId());
        chat.setUpdatedAt(now);
        chatRepository.save(chat);
    }

    private void publishCreated(UUID chatId, Message saved) {
        MessageDto dto = messageMapper.toDto(saved);
        messagingTemplate.convertAndSend(
                "/topic/chat." + chatId,
                WsEvent.of(WsEventType.MESSAGE_CREATED, dto)
        );
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}

