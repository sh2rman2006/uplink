package tech.sh2rman.coreservice.domain.chat.service;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface PresenceService {
    void markOnline(UUID userId, OffsetDateTime now);
    void refreshOnline(UUID userId, OffsetDateTime now);
    void markOffline(UUID userId, OffsetDateTime now);

    boolean isOnline(UUID userId);
}
