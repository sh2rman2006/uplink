package tech.sh2rman.coreservice.domain.common.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class DomainException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    protected DomainException(String code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

}
