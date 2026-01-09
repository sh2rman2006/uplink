package tech.sh2rman.coreservice.websocket.dto.request;

import java.util.UUID;

public record TypingWsRequest(
        UUID chatId,
        boolean typing
) {}
