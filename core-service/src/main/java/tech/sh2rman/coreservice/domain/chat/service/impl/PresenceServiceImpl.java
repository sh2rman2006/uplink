package tech.sh2rman.coreservice.domain.chat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tech.sh2rman.coreservice.domain.chat.service.PresenceService;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PresenceServiceImpl implements PresenceService {

    private static final String KEY_PREFIX = "presence:user:";
    private static final Duration TTL = Duration.ofSeconds(70);

    private final RedisTemplate<String, Object> redis;

    private void upsert(UUID userId, OffsetDateTime now) {
        redis.opsForValue().set(KEY_PREFIX + userId, now.toString(), TTL);
    }

    @Override
    public void markOnline(UUID userId, OffsetDateTime now) {
        upsert(userId, now);
    }

    @Override
    public void refreshOnline(UUID userId, OffsetDateTime now) {
        upsert(userId, now);
    }

    @Override
    public void markOffline(UUID userId, OffsetDateTime now) {
        redis.delete(KEY_PREFIX + userId);
    }

    @Override
    public boolean isOnline(UUID userId) {
        return Boolean.TRUE.equals(redis.hasKey(KEY_PREFIX + userId));
    }
}

