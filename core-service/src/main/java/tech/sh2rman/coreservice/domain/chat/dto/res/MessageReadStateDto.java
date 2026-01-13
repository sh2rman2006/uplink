package tech.sh2rman.coreservice.domain.chat.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class MessageReadStateDto {
    private UUID chatId;

    private UUID lastReadMessageId;
    private OffsetDateTime lastReadAt;

    private long unreadCount;
}
