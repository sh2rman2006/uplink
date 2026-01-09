package tech.sh2rman.coreservice.websocket.dto.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class MessageReadUpToPayload {
    private UUID chatId;
    private UUID userId;
    private UUID upToMessageId;
    private OffsetDateTime readAt;
}
