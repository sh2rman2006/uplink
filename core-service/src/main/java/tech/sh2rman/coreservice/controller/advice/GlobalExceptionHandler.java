package tech.sh2rman.coreservice.controller.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tech.sh2rman.coreservice.domain.common.exception.DomainException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(new ErrorResponse(
                        e.getCode(),
                        e.getMessage()
                ));
    }

    public record ErrorResponse(
            String code,
            String message
    ) {
    }
}

