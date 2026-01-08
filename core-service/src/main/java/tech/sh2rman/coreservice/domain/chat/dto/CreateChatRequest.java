package tech.sh2rman.coreservice.domain.chat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.sh2rman.coreservice.domain.chat.model.ChatType;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

public record CreateChatRequest(
        @NotNull ChatType type,
        @Size(max = 128) String title,
        @Size(max = 2048) String description,
        Set<UUID> memberUserIds
) implements Serializable {
}
