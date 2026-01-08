package tech.sh2rman.coreservice.websocket.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import tech.sh2rman.coreservice.websocket.service.ChatAuthorizationService;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatAclChannelInterceptor implements ChannelInterceptor {

    private final ChatAuthorizationService chatAuthorizationService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);

        log.debug("INBOUND STOMP cmd={} dest={} user={}",
                acc.getCommand(), acc.getDestination(), acc.getUser());

        if (acc.getCommand() == null) return message;

        if (StompCommand.CONNECT.equals(acc.getCommand())) return message;

        String destination = acc.getDestination();
        if (destination == null) return message;

        Authentication user = (Authentication) acc.getUser();
        if (user == null || user.getName() == null) {
            throw new IllegalArgumentException("Unauthenticated websocket session");
        }

        UUID chatId = extractChatId(destination);
        if (chatId == null) return message;

        String keycloakSub = user.getName();

        if (StompCommand.SUBSCRIBE.equals(acc.getCommand())) {
            if (destination.startsWith("/topic/chat.")) {
                chatAuthorizationService.assertCanSubscribe(chatId, keycloakSub);
            }
        }

        if (StompCommand.SEND.equals(acc.getCommand())) {
            if (destination.startsWith("/app/chat.")) {
                chatAuthorizationService.assertCanSend(chatId, keycloakSub);
            }
        }

        return message;
    }

    private UUID extractChatId(String destination) {
        try {
            if (destination.startsWith("/topic/chat.")) {
                String raw = destination.substring("/topic/chat.".length());
                return UUID.fromString(raw);
            }

            if (destination.startsWith("/app/chat.")) {
                String raw = destination.substring("/app/chat.".length());
                int dot = raw.indexOf('.');
                if (dot < 0) return null;
                return UUID.fromString(raw.substring(0, dot));
            }

            return null;
        } catch (Exception ignored) {
            return null;
        }
    }
}
