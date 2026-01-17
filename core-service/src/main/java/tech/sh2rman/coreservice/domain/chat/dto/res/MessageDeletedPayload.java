package tech.sh2rman.coreservice.domain.chat.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@AllArgsConstructor
@Setter
@Getter
public class MessageDeletedPayload{
    private UUID chatId;
    private UUID messageId;
    private OffsetDateTime deletedAt;
}
