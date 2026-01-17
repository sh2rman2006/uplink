package tech.sh2rman.coreservice.domain.chat.mapper;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.sh2rman.coreservice.domain.chat.dto.res.ChatDto;
import tech.sh2rman.coreservice.domain.chat.entity.Chat;
import tech.sh2rman.coreservice.domain.common.mapper.EntitiesMapper;
import tech.sh2rman.coreservice.integration.minio.MinioStorageService;

@Component
@RequiredArgsConstructor
public class ChatMapper implements EntitiesMapper<Chat, ChatDto> {

    private final MinioStorageService storage;

    @Override
    public ChatDto toDto(@NotNull Chat c) {
        ChatDto dto = new ChatDto();

        dto.setId(c.getId());
        dto.setType(c.getType());

        dto.setTitle(safeTrim(c.getTitle()));
        dto.setDescription(safeTrim(c.getDescription()));

        dto.setIsPrivate(c.getIsPrivate());
        dto.setIsPublic(c.getIsPublic());

        dto.setInviteLink(safeTrim(c.getInviteLink()));
        dto.setInviteLinkExpiresAt(c.getInviteLinkExpiresAt());

        dto.setAllowSendMessages(c.getAllowSendMessages());
        dto.setAllowSendMedia(c.getAllowSendMedia());
        dto.setAllowAddUsers(c.getAllowAddUsers());
        dto.setAllowPinMessages(c.getAllowPinMessages());
        dto.setAllowChangeInfo(c.getAllowChangeInfo());

        dto.setIsEncrypted(c.getIsEncrypted());

        if (c.getCreatedBy() != null) {
            dto.setCreatedById(c.getCreatedBy().getId());
        }

        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());

        dto.setLastMessageAt(c.getLastMessageAt());
        dto.setLastMessageId(c.getLastMessageId());

        String objectKey = safeTrim(c.getAvatarObjectKey());
        if (objectKey != null) {
            dto.setAvatarUrl(storage.presignGet(objectKey));
        } else {
            dto.setAvatarUrl(null);
        }

        return dto;
    }

    @Override
    public Chat toEntity(@NotNull ChatDto dto) {
        throw new UnsupportedOperationException(
                "Mapping ChatDto -> Chat is not supported"
        );
    }

    private String safeTrim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
