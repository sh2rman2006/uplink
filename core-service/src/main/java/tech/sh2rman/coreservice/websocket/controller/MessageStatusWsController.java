package tech.sh2rman.coreservice.websocket.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import tech.sh2rman.coreservice.domain.chat.service.MessageStatusService;
import tech.sh2rman.coreservice.websocket.dto.request.MessageDeliveredRequest;
import tech.sh2rman.coreservice.websocket.dto.request.MessageReadUpToRequest;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class MessageStatusWsController {

    private final MessageStatusService messageStatusService;

    @MessageMapping("/chat.message.delivered")
    public void delivered(@Valid MessageDeliveredRequest req, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        messageStatusService.delivered(req.chatId(), userId, req.messageId());
    }

    @MessageMapping("/chat.message.readUpTo")
    public void readUpTo(@Valid MessageReadUpToRequest req, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        messageStatusService.readUpTo(req.chatId(), userId, req.upToMessageId());
    }
}
