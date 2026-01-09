package tech.sh2rman.coreservice.domain.chat.exception;

import org.springframework.http.HttpStatus;
import tech.sh2rman.coreservice.domain.common.exception.DomainException;

import java.util.UUID;

public class MessageNotFoundException extends DomainException {
    public MessageNotFoundException(UUID chatId, UUID messageId) {
        super("MESSAGE_NOT_FOUND", HttpStatus.NOT_FOUND,
                "Message not found: " + messageId + " in chat " + chatId);
    }
}

