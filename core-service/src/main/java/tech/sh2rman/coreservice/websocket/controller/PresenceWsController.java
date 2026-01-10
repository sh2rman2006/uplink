package tech.sh2rman.coreservice.websocket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import tech.sh2rman.coreservice.domain.chat.service.PresenceService;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class PresenceWsController {

    private final PresenceService presenceService;

    @MessageMapping("/presence.ping")
    public void ping(Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        presenceService.refreshOnline(userId, OffsetDateTime.now());
    }

}
