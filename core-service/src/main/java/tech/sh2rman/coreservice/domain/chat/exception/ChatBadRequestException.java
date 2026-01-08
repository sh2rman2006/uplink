package tech.sh2rman.coreservice.domain.chat.exception;

import org.springframework.http.HttpStatus;
import tech.sh2rman.coreservice.domain.common.exception.DomainException;

public class ChatBadRequestException extends DomainException {

    public ChatBadRequestException(String message) {
        super("CHAT_BAD_REQUEST", HttpStatus.BAD_REQUEST, message);
    }
}
