package tech.sh2rman.coreservice.domain.chat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sh2rman.coreservice.domain.chat.dto.CreateTextMessageRequest;
import tech.sh2rman.coreservice.domain.chat.dto.EditTextMessageRequest;
import tech.sh2rman.coreservice.domain.chat.dto.MessageDto;
import tech.sh2rman.coreservice.domain.chat.entity.Chat;
import tech.sh2rman.coreservice.domain.chat.entity.ChatParticipant;
import tech.sh2rman.coreservice.domain.chat.entity.Message;
import tech.sh2rman.coreservice.domain.chat.exception.MessageBadRequestException;
import tech.sh2rman.coreservice.domain.chat.mapper.MessageMapper;
import tech.sh2rman.coreservice.domain.chat.model.MessageStatus;
import tech.sh2rman.coreservice.domain.chat.model.MessageType;
import tech.sh2rman.coreservice.domain.chat.repository.ChatRepository;
import tech.sh2rman.coreservice.domain.chat.repository.MessageRepository;
import tech.sh2rman.coreservice.domain.chat.service.MessageAccessService;
import tech.sh2rman.coreservice.domain.chat.service.TextMessageService;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;
import tech.sh2rman.coreservice.websocket.dto.WsEvent;
import tech.sh2rman.coreservice.websocket.dto.WsEventType;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TextMessageServiceImpl implements TextMessageService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageMapper messageMapper;
    private final MessageAccessService access;

    @Override
    @Transactional
    public MessageDto sendText(UUID chatId, UUID userId, CreateTextMessageRequest req) {

        if (req == null || req.text() == null || req.text().isBlank()) {
            throw new MessageBadRequestException("text is required");
        }

        Chat chat = access.requireChat(chatId);
        ChatParticipant me = access.requireParticipant(chatId, userId);
        UserProfileEntity sender = access.requireUser(userId);

        access.assertCanSend(chat, me);

        OffsetDateTime now = OffsetDateTime.now();

        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);

        message.setType(MessageType.TEXT);
        message.setText(req.text());

        message.setIsForwarded(false);
        message.setStatus(MessageStatus.SENT);
        message.setEditCount(0);

        message.setCreatedAt(now);
        message.setUpdatedAt(now);

        Message saved = messageRepository.save(message);

        chat.setLastMessageAt(now);
        chat.setLastMessageId(saved.getId());
        chat.setUpdatedAt(now);
        chatRepository.save(chat);

        MessageDto dto = messageMapper.toDto(saved);

        messagingTemplate.convertAndSend(
                "/topic/chat." + chatId,
                WsEvent.of(WsEventType.MESSAGE_CREATED, dto)
        );

        return dto;
    }

    @Override
    @Transactional
    public MessageDto editText(UUID chatId, UUID userId, UUID messageId, EditTextMessageRequest req) {

        if (req == null || req.text() == null || req.text().isBlank()) {
            throw new MessageBadRequestException("text is required");
        }

        Chat chat = access.requireChat(chatId);
        ChatParticipant me = access.requireParticipant(chatId, userId);
        Message message = access.requireMessage(chatId, messageId);

        access.assertCanEditText(chat, me, message, userId);

        if (message.getDeletedAt() != null) {
            throw new MessageBadRequestException("message is deleted");
        }

        OffsetDateTime now = OffsetDateTime.now();

        message.setText(req.text());
        message.setUpdatedAt(now);
        message.setLastEditedAt(now);
        message.setEditCount((message.getEditCount() == null ? 1 : message.getEditCount()) + 1);

        Message saved = messageRepository.save(message);

        MessageDto dto = messageMapper.toDto(saved);

        messagingTemplate.convertAndSend(
                "/topic/chat." + chatId,
                WsEvent.of(WsEventType.MESSAGE_EDITED, dto)
        );

        if (chat.getLastMessageId() != null && chat.getLastMessageId().equals(messageId)) {
            chat.setUpdatedAt(now);
            chatRepository.save(chat);
        }

        return dto;
    }
}
