package tech.sh2rman.coreservice.domain.chat.exception;

import org.springframework.http.HttpStatus;
import tech.sh2rman.coreservice.domain.common.exception.DomainException;

import java.util.UUID;

public class ChatNotFoundException extends DomainException {
    public ChatNotFoundException(UUID chatId) {
        super("CHAT_NOT_FOUND", HttpStatus.NOT_FOUND, "Chat not found: " + chatId);
    }

    public ChatNotFoundException(String message) {
        super(
                "CHAT_NOT_FOUND",
                HttpStatus.NOT_FOUND,
                message
        );
    }
}
