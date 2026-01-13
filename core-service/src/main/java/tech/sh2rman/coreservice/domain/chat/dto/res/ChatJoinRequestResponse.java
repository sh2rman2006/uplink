package tech.sh2rman.coreservice.domain.chat.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChatJoinRequestResponse {

    private UUID id;
    private UUID chatId;
    private UUID userId;

    private String status;
    private String message;

    private UUID reviewedById;
    private OffsetDateTime reviewedAt;

    private OffsetDateTime createdAt;
}
