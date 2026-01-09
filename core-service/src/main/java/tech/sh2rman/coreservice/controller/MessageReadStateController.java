package tech.sh2rman.coreservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.sh2rman.coreservice.domain.chat.dto.MessageReadStateDto;
import tech.sh2rman.coreservice.domain.chat.service.MessageReadStateService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
@Tag(name = "Read State", description = "Состояние прочтения сообщений")
public class MessageReadStateController {

    private final MessageReadStateService readStateService;

    @Operation(summary = "Моё состояние прочтения в чате (lastReadMessageId/lastReadAt)")
    @GetMapping("/{chatId}/read-state")
    public MessageReadStateDto getMyReadState(
            @PathVariable UUID chatId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return readStateService.getMyReadState(chatId, userId);
    }
}
