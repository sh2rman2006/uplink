package tech.sh2rman.coreservice.domain.chat.service;

import java.util.UUID;

public interface MessageStatusService {
    void delivered(UUID chatId, UUID userId, UUID messageId);

    void readUpTo(UUID chatId, UUID userId, UUID upToMessageId);
}
