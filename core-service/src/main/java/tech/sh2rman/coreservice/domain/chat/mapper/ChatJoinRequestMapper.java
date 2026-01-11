package tech.sh2rman.coreservice.domain.chat.mapper;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import tech.sh2rman.coreservice.domain.chat.dto.ChatJoinRequestResponse;
import tech.sh2rman.coreservice.domain.chat.entity.ChatJoinRequest;
import tech.sh2rman.coreservice.domain.common.mapper.EntitiesMapper;

@Component
public class ChatJoinRequestMapper implements EntitiesMapper<ChatJoinRequest, ChatJoinRequestResponse> {

    @Override
    public ChatJoinRequestResponse toDto(@NotNull ChatJoinRequest jr) {
        ChatJoinRequestResponse r = new ChatJoinRequestResponse();
        r.setId(jr.getId());
        r.setStatus(jr.getStatus());
        r.setMessage(trimOrNull(jr.getMessage()));

        if (jr.getChat() != null) r.setChatId(jr.getChat().getId());
        if (jr.getUser() != null) r.setUserId(jr.getUser().getId());

        if (jr.getReviewedBy() != null) r.setReviewedById(jr.getReviewedBy().getId());
        r.setReviewedAt(jr.getReviewedAt());
        r.setCreatedAt(jr.getCreatedAt());
        return r;
    }

    @Override
    public ChatJoinRequest toEntity(@NotNull ChatJoinRequestResponse dto) {
        throw new UnsupportedOperationException(
                "Mapping ChatJoinRequestResponse -> ChatJoinRequest is not supported"
        );
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}