package tech.sh2rman.coreservice.domain.chat.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageDeletedPayload(
        UUID chatId,
        UUID messageId,
        OffsetDateTime deletedAt
) {
}
