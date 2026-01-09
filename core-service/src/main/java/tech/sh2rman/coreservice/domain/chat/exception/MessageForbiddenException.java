package tech.sh2rman.coreservice.domain.chat.exception;

import org.springframework.http.HttpStatus;
import tech.sh2rman.coreservice.domain.common.exception.DomainException;

public class MessageForbiddenException extends DomainException {

    public MessageForbiddenException(String message) {
        super("ACCESS_DENIED", HttpStatus.FORBIDDEN, message);
    }
}