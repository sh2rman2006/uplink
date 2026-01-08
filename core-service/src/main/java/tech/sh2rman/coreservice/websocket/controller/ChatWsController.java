package tech.sh2rman.coreservice.websocket.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import tech.sh2rman.coreservice.domain.chat.dto.SendMessageWsRequest;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWsController {

    @MessageMapping("/chat.{chatId}.send")
    public void sendMessage(
            @DestinationVariable UUID chatId,
            @Payload SendMessageWsRequest payload,
            Authentication authentication
    ) {
        String userSub = authentication.getName();

        log.info(
                "WS MESSAGE | chatId={} | user={} | payload={}",
                chatId,
                userSub,
                payload
        );
    }
}
