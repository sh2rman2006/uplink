package tech.sh2rman.coreservice.websocket.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MessageDeliveredRequest(
        @NotNull
        UUID chatId,
        @NotNull
        UUID messageId
) {}
