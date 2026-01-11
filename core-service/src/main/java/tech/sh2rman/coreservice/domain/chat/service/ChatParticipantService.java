package tech.sh2rman.coreservice.domain.chat.service;

import tech.sh2rman.coreservice.domain.chat.model.ChatRole;

import java.util.UUID;

public interface ChatParticipantService {
    void add(UUID chatId, UUID actorUserId, UUID targetUserId, ChatRole role);

    void kick(UUID chatId, UUID actorUserId, UUID targetUserId);

    void leave(UUID chatId, UUID actorUserId);

    void changeRole(UUID chatId, UUID actorUserId, UUID targetUserId, ChatRole role);
}
