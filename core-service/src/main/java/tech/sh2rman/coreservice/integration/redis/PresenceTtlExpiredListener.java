package tech.sh2rman.coreservice.integration.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tech.sh2rman.coreservice.domain.chat.service.PresenceService;
import tech.sh2rman.coreservice.websocket.dto.WsEvent;
import tech.sh2rman.coreservice.websocket.dto.WsEventType;
import tech.sh2rman.coreservice.websocket.dto.payload.UserOfflinePayload;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class PresenceTtlExpiredListener implements MessageListener {

    private static final String KEY_PREFIX = "presence:user:";

    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = new String(message.getBody(), StandardCharsets.UTF_8);

        if (!expiredKey.startsWith(KEY_PREFIX)) return;

        String rawUserId = expiredKey.substring(KEY_PREFIX.length());
        UUID userId;
        try {
            userId = UUID.fromString(rawUserId);
        } catch (Exception e) {
            log.warn("Bad presence key expired: {}", expiredKey);
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();

        presenceService.markOffline(userId, now);

        messagingTemplate.convertAndSend(
                "/topic/user." + userId + ".presence",
                WsEvent.of(WsEventType.USER_OFFLINE, new UserOfflinePayload(userId, now))
        );

    }
}

