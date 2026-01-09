package tech.sh2rman.coreservice.domain.chat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sh2rman.coreservice.domain.chat.entity.Chat;
import tech.sh2rman.coreservice.domain.chat.entity.ChatParticipant;
import tech.sh2rman.coreservice.domain.chat.entity.Message;
import tech.sh2rman.coreservice.domain.chat.exception.MessageBadRequestException;
import tech.sh2rman.coreservice.domain.chat.model.ChatType;
import tech.sh2rman.coreservice.domain.chat.model.MessageStatus;
import tech.sh2rman.coreservice.domain.chat.repository.ChatParticipantRepository;
import tech.sh2rman.coreservice.domain.chat.repository.MessageRepository;
import tech.sh2rman.coreservice.domain.chat.service.MessageAccessService;
import tech.sh2rman.coreservice.domain.chat.service.MessageStatusService;
import tech.sh2rman.coreservice.websocket.dto.WsEvent;
import tech.sh2rman.coreservice.websocket.dto.WsEventType;
import tech.sh2rman.coreservice.websocket.dto.payload.MessageDeliveredPayload;
import tech.sh2rman.coreservice.websocket.dto.payload.MessageReadUpToPayload;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageStatusServiceImpl implements MessageStatusService {
    private final MessageRepository messageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final MessageAccessService access;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void delivered(UUID chatId, UUID userId, UUID messageId) {

        ChatParticipant me = access.requireParticipant(chatId, userId);
        access.assertCanRead(me);

        Message message = access.requireMessage(chatId, messageId);
        if (message.getDeletedAt() != null) {
            throw new MessageBadRequestException("message is deleted");
        }

        OffsetDateTime now = OffsetDateTime.now();
        Chat chat = message.getChat();

        if (chat.getType() == ChatType.PRIVATE) {
            MessageStatus st = message.getStatus();
            if (st == null || st == MessageStatus.SENT) {
                message.setStatus(MessageStatus.DELIVERED);
                message.setUpdatedAt(now);
                messageRepository.save(message);
            }
        }

        messagingTemplate.convertAndSend(
                "/topic/chat." + chatId,
                WsEvent.of(
                        WsEventType.MESSAGE_DELIVERED,
                        new MessageDeliveredPayload(chatId, messageId, userId, now)
                )
        );
    }

    @Override
    @Transactional
    public void readUpTo(UUID chatId, UUID userId, UUID upToMessageId) {

        ChatParticipant me = access.requireParticipant(chatId, userId);
        access.assertCanRead(me);

        Message upTo = access.requireMessage(chatId, upToMessageId);
        if (upTo.getDeletedAt() != null) {
            throw new MessageBadRequestException("message is deleted");
        }

        OffsetDateTime now = OffsetDateTime.now();
        Chat chat = upTo.getChat();

        UUID currentLastId = me.getLastReadMessageId();
        if (currentLastId != null && !currentLastId.equals(upToMessageId)) {
            Message currentLast = messageRepository.findByIdAndChatId(currentLastId, chatId).orElse(null);
            if (currentLast != null) {
                // если новое upTo НЕ позже текущего — ничего не делаем
                if (!upTo.getCreatedAt().isAfter(currentLast.getCreatedAt())) {
                    return;
                }
            }
        }

        me.setLastReadMessageId(upToMessageId);
        me.setLastReadAt(now);
        me.setUpdatedAt(now);
        chatParticipantRepository.save(me);

        if (chat.getType() == ChatType.PRIVATE) {
            if (upTo.getStatus() != MessageStatus.READ) {
                upTo.setStatus(MessageStatus.READ);
                upTo.setUpdatedAt(now);
                messageRepository.save(upTo);
            }
        }

        messagingTemplate.convertAndSend(
                "/topic/chat." + chatId,
                WsEvent.of(
                        WsEventType.MESSAGE_READ,
                        new MessageReadUpToPayload(chatId, userId, upToMessageId, now)
                )
        );
    }

}
