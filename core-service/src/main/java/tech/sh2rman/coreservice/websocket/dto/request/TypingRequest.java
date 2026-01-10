package tech.sh2rman.coreservice.websocket.dto.request;

import jakarta.validation.constraints.NotNull;

public record TypingRequest(@NotNull boolean typing) {}
