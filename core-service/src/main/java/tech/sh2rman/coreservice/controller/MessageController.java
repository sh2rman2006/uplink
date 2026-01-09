package tech.sh2rman.coreservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import tech.sh2rman.coreservice.domain.chat.dto.CreateTextMessageRequest;
import tech.sh2rman.coreservice.domain.chat.dto.EditTextMessageRequest;
import tech.sh2rman.coreservice.domain.chat.dto.MessageDto;
import tech.sh2rman.coreservice.domain.chat.service.MessageDeleteService;
import tech.sh2rman.coreservice.domain.chat.service.MessageQueryService;
import tech.sh2rman.coreservice.domain.chat.service.TextMessageService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "Message", description = "API сообщений")
public class MessageController {

    private final TextMessageService textMessageService;
    private final MessageQueryService messageQueryService;
    private final MessageDeleteService messageDeleteService;

    @Operation(summary = "Отправить текстовое сообщение")
    @PostMapping("/{chatId}/message/text")
    public MessageDto sendText(
            @PathVariable UUID chatId,
            @Valid @RequestBody CreateTextMessageRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return textMessageService.sendText(chatId, userId, req);
    }

    @Operation(summary = "Получить сообщения чата (последние 50; пагинация по before)")
    @GetMapping("/{chatId}/messages")
    public List<MessageDto> list(
            @PathVariable UUID chatId,
            @RequestParam(required = false) OffsetDateTime before,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return messageQueryService.list(chatId, userId, before);
    }

    @Operation(summary = "Редактировать текст сообщения")
    @PatchMapping("/{chatId}/message/{messageId}/text")
    public MessageDto editText(
            @PathVariable UUID chatId,
            @PathVariable UUID messageId,
            @Valid @RequestBody EditTextMessageRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return textMessageService.editText(chatId, userId, messageId, req);
    }

    @Operation(summary = "Удалить сообщение (у всех)")
    @DeleteMapping("/{chatId}/message/{messageId}")
    public void delete(
            @PathVariable UUID chatId,
            @PathVariable UUID messageId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        messageDeleteService.delete(chatId, userId, messageId);
    }
}