package tech.sh2rman.coreservice.domain.chat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tech.sh2rman.coreservice.domain.chat.service.TypingService;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TypingServiceImpl implements TypingService {

    private static final String KEY_PREFIX = "typing:chat:";
    private static final Duration TTL = Duration.ofSeconds(6);

    private final RedisTemplate<String, Object> redis;

    private String key(UUID chatId, UUID userId) {
        return KEY_PREFIX + chatId + ":user:" + userId;
    }

    @Override
    public void setTyping(UUID chatId, UUID userId, OffsetDateTime now) {
        redis.opsForValue().set(key(chatId, userId), now.toString(), TTL);
    }

    @Override
    public void stopTyping(UUID chatId, UUID userId) {
        redis.delete(key(chatId, userId));
    }
}
