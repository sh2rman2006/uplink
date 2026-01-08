package tech.sh2rman.coreservice.domain.chat.exception;

import org.springframework.http.HttpStatus;
import tech.sh2rman.coreservice.domain.common.exception.DomainException;

public class MessageBadRequestException extends DomainException {

    public MessageBadRequestException(String message) {
        super("MESSAGE_BAD_REQUEST", HttpStatus.BAD_REQUEST, message);
    }
}

