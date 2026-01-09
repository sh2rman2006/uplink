package tech.sh2rman.coreservice.domain.chat.exception;

import org.springframework.http.HttpStatus;
import tech.sh2rman.coreservice.domain.common.exception.DomainException;

import java.util.UUID;

public class ChatParticipantNotFoundException extends DomainException {
    public ChatParticipantNotFoundException(UUID userId, UUID chatId) {
        super("CHAT_PARTICIPANT_NOT_FOUND", HttpStatus.NOT_FOUND, "Chat participant not found, user: " + userId + ", chat: " + chatId);
    }
}