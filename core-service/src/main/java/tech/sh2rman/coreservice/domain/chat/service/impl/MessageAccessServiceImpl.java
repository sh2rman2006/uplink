package tech.sh2rman.coreservice.domain.chat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.sh2rman.coreservice.domain.chat.entity.Chat;
import tech.sh2rman.coreservice.domain.chat.entity.ChatParticipant;
import tech.sh2rman.coreservice.domain.chat.entity.Message;
import tech.sh2rman.coreservice.domain.chat.exception.*;
import tech.sh2rman.coreservice.domain.chat.model.ChatRole;
import tech.sh2rman.coreservice.domain.chat.model.ChatType;
import tech.sh2rman.coreservice.domain.chat.model.ParticipantStatus;
import tech.sh2rman.coreservice.domain.chat.repository.ChatParticipantRepository;
import tech.sh2rman.coreservice.domain.chat.repository.ChatRepository;
import tech.sh2rman.coreservice.domain.chat.repository.MessageRepository;
import tech.sh2rman.coreservice.domain.chat.service.MessageAccessService;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;
import tech.sh2rman.coreservice.domain.user.exception.UserProfileNotFoundException;
import tech.sh2rman.coreservice.domain.user.repository.UserProfileRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageAccessServiceImpl implements MessageAccessService {

    private final ChatRepository chatRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final MessageRepository messageRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    public Chat requireChat(UUID chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException(chatId));
    }

    @Override
    public ChatParticipant requireParticipant(UUID chatId, UUID userId) {
        return chatParticipantRepository.findByChatIdAndUserId(chatId, userId)
                .orElseThrow(() -> new ChatParticipantNotFoundException(userId, chatId));
    }

    @Override
    public UserProfileEntity requireUser(UUID userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(UserProfileNotFoundException::new);
    }

    @Override
    public Message requireMessage(UUID chatId, UUID messageId) {
        return messageRepository.findByIdAndChatId(messageId, chatId)
                .orElseThrow(() -> new MessageNotFoundException(chatId, messageId));
    }

    @Override
    public void assertCanRead(ChatParticipant me) {
        if (me.getStatus() != ParticipantStatus.ACTIVE) {
            throw new MessageForbiddenException("Participant not active");
        }

        if (me.getRole() == ChatRole.BANNED) {
            throw new MessageForbiddenException("Banned");
        }

        OffsetDateTime bannedUntil = me.getBannedUntil();
        if (bannedUntil != null && bannedUntil.isAfter(OffsetDateTime.now())) {
            throw new MessageForbiddenException("Participant is banned");
        }
    }

    @Override
    public void assertCanSend(Chat chat, ChatParticipant me) {
        assertCanRead(me);

        if (chat.getType() != ChatType.PRIVATE) {
            if (me.getRole() == ChatRole.READER) {
                throw new MessageForbiddenException("Read-only");
            }
            if (Boolean.FALSE.equals(chat.getAllowSendMessages())) {
                throw new MessageForbiddenException("Chat does not allow sending messages");
            }
            if (Boolean.FALSE.equals(me.getCanSendMessages())) {
                throw new MessageForbiddenException("Not allowed to send messages");
            }
        }
    }


    @Override
    public void assertCanEditText(Chat chat, ChatParticipant me, Message message, UUID userId) {
        assertCanRead(me);

        if (chat.getType() != ChatType.PRIVATE && me.getRole() == ChatRole.READER) {
            throw new MessageForbiddenException("Read-only");
        }

        if (message.getDeletedAt() != null) {
            throw new MessageBadRequestException("message is deleted");
        }

        boolean isSender = message.getSender() != null
                && message.getSender().getId() != null
                && message.getSender().getId().equals(userId);

        boolean canEdit = switch (chat.getType()) {
            case PRIVATE, GROUP -> isSender;
            case CHANNEL, SECRET -> false;
        };

        if (!canEdit) {
            throw new MessageForbiddenException("Not allowed to edit this message");
        }
    }

    @Override
    public void assertCanDelete(Chat chat, ChatParticipant me, Message message, UUID userId) {
        assertCanRead(me);

        boolean isSender = message.getSender() != null
                && message.getSender().getId() != null
                && message.getSender().getId().equals(userId);

        boolean canDelete = switch (chat.getType()) {
            case PRIVATE -> true;
            case GROUP -> isSender || me.getRole() == ChatRole.OWNER || me.getRole() == ChatRole.ADMIN;
            case CHANNEL, SECRET -> false;
        };

        if (!canDelete) {
            throw new MessageForbiddenException("Not allowed to delete this message");
        }
    }
}