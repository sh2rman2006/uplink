package tech.sh2rman.coreservice.domain.chat.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.sh2rman.coreservice.domain.chat.model.ChatRole;
import tech.sh2rman.coreservice.domain.chat.model.ParticipantStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChatParticipantResponse {

    private UUID userId;

    private String username;
    private String displayName;
    private String avatarUrl;
    private Long avatarVersion;
    private OffsetDateTime lastSeenAt;

    private ChatRole role;
    private ParticipantStatus status;

    private OffsetDateTime joinedAt;

}
