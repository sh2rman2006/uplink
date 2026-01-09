package tech.sh2rman.coreservice.domain.chat.service;

import java.util.UUID;

public interface MessageDeleteService {
    void delete(UUID chatId, UUID userId, UUID messageId);
}
