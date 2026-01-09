package tech.sh2rman.coreservice.websocket.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import tech.sh2rman.coreservice.domain.chat.service.MessageStatusService;
import tech.sh2rman.coreservice.websocket.dto.request.MessageDeliveredRequest;
import tech.sh2rman.coreservice.websocket.dto.request.MessageReadUpToRequest;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class MessageStatusWsController {
    private final MessageStatusService messageStatusService;

    @MessageMapping("/chat.message.delivered")
    public void delivered(@Valid MessageDeliveredRequest req, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        messageStatusService.delivered(req.chatId(), userId, req.messageId());
    }

    @MessageMapping("/chat.message.readUpTo")
    public void readUpTo(@Valid MessageReadUpToRequest req, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        messageStatusService.readUpTo(req.chatId(), userId, req.upToMessageId());
    }

}
