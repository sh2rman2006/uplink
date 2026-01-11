package tech.sh2rman.coreservice.domain.chat.dto;

import jakarta.validation.constraints.NotNull;
import tech.sh2rman.coreservice.domain.chat.model.ChatRole;

import java.util.UUID;

public record AddChatParticipantRequest(@NotNull UUID userId, ChatRole role) {
}
