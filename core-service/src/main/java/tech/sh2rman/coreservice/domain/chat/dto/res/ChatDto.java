package tech.sh2rman.coreservice.domain.chat.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.sh2rman.coreservice.domain.chat.model.ChatType;

import java.time.OffsetDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChatDto {

    private UUID id;

    private ChatType type;

    private String title;
    private String description;

    private String avatarUrl;

    private Boolean isPrivate;
    private Boolean isPublic;

    private String inviteLink;
    private OffsetDateTime inviteLinkExpiresAt;

    private Boolean allowSendMessages;
    private Boolean allowSendMedia;
    private Boolean allowAddUsers;
    private Boolean allowPinMessages;
    private Boolean allowChangeInfo;

    private Boolean isEncrypted;

    private UUID createdById;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private OffsetDateTime lastMessageAt;
    private UUID lastMessageId;
}
