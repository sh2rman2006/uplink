package tech.sh2rman.coreservice.domain.chat.dto.req;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record InviteToChatRequest(
        @NotNull UUID userId,
        @Size(max = 280) String message
) {}
