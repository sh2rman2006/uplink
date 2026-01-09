package tech.sh2rman.coreservice.domain.user.exception;

import org.springframework.http.HttpStatus;
import tech.sh2rman.coreservice.domain.common.exception.DomainException;

public class UserProfileNotFoundException extends DomainException {
    public UserProfileNotFoundException() {
        super("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "User profile not found");
    }
}
