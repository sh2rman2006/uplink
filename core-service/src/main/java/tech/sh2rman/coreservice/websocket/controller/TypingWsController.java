package tech.sh2rman.coreservice.websocket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import tech.sh2rman.coreservice.domain.chat.entity.Chat;
import tech.sh2rman.coreservice.domain.chat.entity.ChatParticipant;
import tech.sh2rman.coreservice.domain.chat.service.MessageAccessService;
import tech.sh2rman.coreservice.domain.chat.service.TypingService;
import tech.sh2rman.coreservice.websocket.dto.WsEvent;
import tech.sh2rman.coreservice.websocket.dto.WsEventType;
import tech.sh2rman.coreservice.websocket.dto.payload.UserStopTypingPayload;
import tech.sh2rman.coreservice.websocket.dto.payload.UserTypingPayload;
import tech.sh2rman.coreservice.websocket.dto.request.TypingRequest;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class TypingWsController {

    private final MessageAccessService access;
    private final TypingService typingService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.{chatId}.typing")
    public void typing(@DestinationVariable UUID chatId,
                       Principal principal,
                       TypingRequest req) {

        UUID userId = UUID.fromString(principal.getName());

        Chat chat = access.requireChat(chatId);
        ChatParticipant me = access.requireParticipant(chatId, userId);
        access.assertCanSend(chat, me);

        OffsetDateTime now = OffsetDateTime.now();

        if (req != null && req.typing()) {
            typingService.setTyping(chatId, userId, now);

            messagingTemplate.convertAndSend(
                    "/topic/chat." + chatId,
                    WsEvent.of(WsEventType.USER_TYPING, new UserTypingPayload(chatId, userId, now))
            );
        } else {
            typingService.stopTyping(chatId, userId);

            messagingTemplate.convertAndSend(
                    "/topic/chat." + chatId,
                    WsEvent.of(WsEventType.USER_STOP_TYPING, new UserStopTypingPayload(chatId, userId, now))
            );
        }
    }
}

