package tech.sh2rman.coreservice.websocket.dto.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserOfflinePayload {
    private UUID userId;
    private OffsetDateTime at;
}
