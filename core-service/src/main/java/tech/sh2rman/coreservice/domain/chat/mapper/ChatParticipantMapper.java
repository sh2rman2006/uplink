package tech.sh2rman.coreservice.domain.chat.mapper;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.sh2rman.coreservice.domain.chat.dto.res.ChatParticipantResponse;
import tech.sh2rman.coreservice.domain.chat.entity.ChatParticipant;
import tech.sh2rman.coreservice.domain.common.mapper.EntitiesMapper;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;
import tech.sh2rman.coreservice.integration.minio.MinioStorageService;

@Component
@RequiredArgsConstructor
public class ChatParticipantMapper implements EntitiesMapper<ChatParticipant, ChatParticipantResponse> {
    private final MinioStorageService minioStorageService;

    @Override
    public ChatParticipantResponse toDto(@NotNull ChatParticipant p) {
        ChatParticipantResponse r = new ChatParticipantResponse();

        UserProfileEntity u = p.getUser();
        if (u != null) {
            r.setUserId(u.getId());
            r.setUsername(u.getUsername());
            r.setDisplayName(u.getDisplayName());
            if (u.getAvatarObjectKey() != null) {
                r.setAvatarUrl(minioStorageService.presignGet(u.getAvatarObjectKey()));
            }
            r.setAvatarVersion(u.getAvatarVersion());
            r.setLastSeenAt(u.getLastSeenAt());
        }

        r.setRole(p.getRole());
        r.setStatus(p.getStatus());
        r.setJoinedAt(p.getJoinedAt());

        return r;
    }

    @Override
    public ChatParticipant toEntity(@NotNull ChatParticipantResponse dto) {
        throw new UnsupportedOperationException(
                "Mapping ChatParticipantResponse -> ChatParticipant is not supported"
        );
    }
}