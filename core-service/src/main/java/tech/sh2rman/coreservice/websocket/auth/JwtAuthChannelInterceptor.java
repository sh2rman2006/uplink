package tech.sh2rman.coreservice.websocket.auth;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthChannelInterceptor implements ChannelInterceptor {

    private static final String SESSION_AUTH_KEY = "WS_AUTH";

    private final JwtDecoder jwtDecoder;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);
        StompCommand cmd = acc.getCommand();
        if (cmd == null) return message;

        if (StompCommand.CONNECT.equals(cmd)) {
            Authentication auth = decodeAuthFromConnectHeader(acc);
            acc.setUser(auth);

            Map<String, Object> attrs = acc.getSessionAttributes();
            if (attrs != null) attrs.put(SESSION_AUTH_KEY, auth);

            return MessageBuilder.createMessage(message.getPayload(), acc.getMessageHeaders());
        }

        if (acc.getUser() == null) {
            Map<String, Object> attrs = acc.getSessionAttributes();
            if (attrs != null) {
                Object stored = attrs.get(SESSION_AUTH_KEY);
                if (stored instanceof Authentication auth) {
                    acc.setUser(auth);
                    return MessageBuilder.createMessage(message.getPayload(), acc.getMessageHeaders());
                }
            }
        }

        return message;
    }

    private Authentication decodeAuthFromConnectHeader(StompHeaderAccessor acc) {
        String header = acc.getFirstNativeHeader("Authorization");
        if (!StringUtils.hasText(header)) {
            throw new IllegalArgumentException("Missing Authorization header");
        }

        String raw = header.trim();
        if (!raw.startsWith("Bearer ")) { // оставим строго, как у тебя было
            throw new IllegalArgumentException("Authorization must start with 'Bearer '");
        }

        String token = raw.substring("Bearer ".length()).trim();
        Jwt jwt = jwtDecoder.decode(token);

        String sub = jwt.getSubject();
        if (!StringUtils.hasText(sub)) {
            throw new IllegalArgumentException("JWT subject is empty");
        }

        return new UsernamePasswordAuthenticationToken(sub, null, List.of());
    }
}