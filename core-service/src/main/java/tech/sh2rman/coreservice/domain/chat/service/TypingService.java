package tech.sh2rman.coreservice.domain.chat.service;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface TypingService {
    void setTyping(UUID chatId, UUID userId, OffsetDateTime now);
    void stopTyping(UUID chatId, UUID userId);
}
