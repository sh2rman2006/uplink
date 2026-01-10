package tech.sh2rman.coreservice.websocket.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import tech.sh2rman.coreservice.domain.chat.service.PresenceService;
import tech.sh2rman.coreservice.websocket.dto.WsEvent;
import tech.sh2rman.coreservice.websocket.dto.WsEventType;
import tech.sh2rman.coreservice.websocket.dto.payload.UserOfflinePayload;
import tech.sh2rman.coreservice.websocket.dto.payload.UserOnlinePayload;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PresenceWsSessionListener {

    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onConnect(SessionConnectEvent event) {
        UUID userId = resolveUserId(event);
        if (userId == null) {
            log.warn("presence: CONNECT without user (no simpUser and no WS_AUTH)");
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();

        boolean wasOnline = presenceService.isOnline(userId);
        presenceService.markOnline(userId, now);

        if (!wasOnline) {
            messagingTemplate.convertAndSend(
                    "/topic/user." + userId + ".presence",
                    WsEvent.of(WsEventType.USER_ONLINE, new UserOnlinePayload(userId, now))
            );
        }
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        UUID userId = resolveUserId(event);
        if (userId == null) {
            log.warn("presence: DISCONNECT without user");
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();

        boolean wasOnline = presenceService.isOnline(userId);
        presenceService.markOffline(userId, now);

        if (wasOnline) {
            messagingTemplate.convertAndSend(
                    "/topic/user." + userId + ".presence",
                    WsEvent.of(WsEventType.USER_OFFLINE, new UserOfflinePayload(userId, now))
            );
        }
    }

    private UUID resolveUserId(Object wsEvent) {
        StompHeaderAccessor accessor;
        if (wsEvent instanceof SessionConnectEvent sce) {
            accessor = StompHeaderAccessor.wrap(sce.getMessage());
        } else if (wsEvent instanceof SessionDisconnectEvent sde) {
            accessor = StompHeaderAccessor.wrap(sde.getMessage());
        } else {
            return null;
        }

        Principal p = accessor.getUser();
        if (p != null) {
            return safeUuid(p.getName());
        }

        Map<String, Object> sess = accessor.getSessionAttributes();
        if (sess == null) return null;

        Object wsAuth = sess.get("WS_AUTH");
        if (wsAuth instanceof Authentication auth && auth.getName() != null) {
            return safeUuid(auth.getName());
        }

        if (wsAuth instanceof Principal pp && pp.getName() != null) {
            return safeUuid(pp.getName());
        }

        return null;
    }

    private UUID safeUuid(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (Exception e) {
            log.warn("presence: cannot parse UUID from principal name='{}'", raw);
            return null;
        }
    }
}
