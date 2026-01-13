package tech.sh2rman.coreservice.domain.chat.mapper;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import tech.sh2rman.coreservice.domain.chat.dto.res.ChatParticipantResponse;
import tech.sh2rman.coreservice.domain.chat.entity.ChatParticipant;
import tech.sh2rman.coreservice.domain.common.mapper.EntitiesMapper;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;

@Component
public class ChatParticipantMapper implements EntitiesMapper<ChatParticipant, ChatParticipantResponse> {

    @Override
    public ChatParticipantResponse toDto(@NotNull ChatParticipant p) {
        ChatParticipantResponse r = new ChatParticipantResponse();

        UserProfileEntity u = p.getUser();
        if (u != null) {
            r.setUserId(u.getId());
            r.setUsername(u.getUsername());
            r.setDisplayName(u.getDisplayName());
            r.setAvatarUrl(u.getAvatarUrl());
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