package tech.sh2rman.coreservice.domain.chat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sh2rman.coreservice.domain.chat.dto.res.MessageDeletedPayload;
import tech.sh2rman.coreservice.domain.chat.entity.Chat;
import tech.sh2rman.coreservice.domain.chat.entity.ChatParticipant;
import tech.sh2rman.coreservice.domain.chat.entity.Message;
import tech.sh2rman.coreservice.domain.chat.repository.ChatRepository;
import tech.sh2rman.coreservice.domain.chat.repository.MessageRepository;
import tech.sh2rman.coreservice.domain.chat.service.ChatAccessService;
import tech.sh2rman.coreservice.domain.chat.service.MessageDeleteService;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;
import tech.sh2rman.coreservice.websocket.dto.WsEvent;
import tech.sh2rman.coreservice.websocket.dto.WsEventType;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageDeleteServiceImpl implements MessageDeleteService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatAccessService access;

    @Override
    @Transactional
    public void delete(UUID chatId, UUID userId, UUID messageId) {

        Chat chat = access.requireChat(chatId);
        ChatParticipant me = access.requireParticipant(chatId, userId);
        Message message = access.requireMessage(chatId, messageId);

        UserProfileEntity actor = access.requireUser(userId);

        access.assertCanDelete(chat, me, message, userId);

        if (message.getDeletedAt() != null) return;

        OffsetDateTime now = OffsetDateTime.now();
        message.setDeletedAt(now);
        message.setDeletedBy(actor);
        message.setUpdatedAt(now);

        messageRepository.save(message);

        if (chat.getLastMessageId() != null && chat.getLastMessageId().equals(messageId)) {
            messageRepository.findFirstByChatIdAndDeletedAtIsNullOrderByCreatedAtDesc(chatId)
                    .ifPresentOrElse(m -> {
                        chat.setLastMessageId(m.getId());
                        chat.setLastMessageAt(m.getCreatedAt());
                    }, () -> {
                        chat.setLastMessageId(null);
                        chat.setLastMessageAt(null);
                    });

            chat.setUpdatedAt(now);
            chatRepository.save(chat);
        }

        messagingTemplate.convertAndSend(
                "/topic/chat." + chatId,
                WsEvent.of(
                        WsEventType.MESSAGE_DELETED,
                        new MessageDeletedPayload(chatId, messageId, now)
                )
        );
    }

}
