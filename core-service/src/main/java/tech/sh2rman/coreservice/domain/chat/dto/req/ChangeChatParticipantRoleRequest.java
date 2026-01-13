package tech.sh2rman.coreservice.domain.chat.dto.req;


import jakarta.validation.constraints.NotNull;
import tech.sh2rman.coreservice.domain.chat.model.ChatRole;

public record ChangeChatParticipantRoleRequest(
        @NotNull ChatRole role
) {}
