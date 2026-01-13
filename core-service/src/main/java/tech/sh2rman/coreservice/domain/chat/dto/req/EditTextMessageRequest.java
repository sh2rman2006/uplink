package tech.sh2rman.coreservice.domain.chat.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditTextMessageRequest(
        @NotBlank
        @Size(max = 10000)
        String text
) {
}

