package tech.sh2rman.coreservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import tech.sh2rman.coreservice.domain.chat.dto.ChatJoinRequestResponse;
import tech.sh2rman.coreservice.domain.chat.dto.InviteToChatRequest;
import tech.sh2rman.coreservice.domain.chat.mapper.ChatJoinRequestMapper;
import tech.sh2rman.coreservice.domain.chat.service.ChatJoinRequestService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
@Tag(name = "Chat join requests", description = "Приглашения/заявки на вступление в чат")
public class ChatJoinRequestController {

    private final ChatJoinRequestService chatJoinRequestService;
    private final ChatJoinRequestMapper mapper;

    @Operation(summary = "Пригласить пользователя в чат")
    @PostMapping("/{chatId}/join-requests/invite")
    public ChatJoinRequestResponse invite(
            @PathVariable UUID chatId,
            @Valid @RequestBody InviteToChatRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID actorId = UUID.fromString(jwt.getSubject());
        return mapper.toDto(chatJoinRequestService.invite(chatId, actorId, req));
    }

    @Operation(summary = "Мои входящие приглашения (inbox)")
    @GetMapping("/join-requests/inbox")
    public Page<ChatJoinRequestResponse> inbox(
            @RequestParam(required = false) String status,
            Pageable pageable,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return chatJoinRequestService.inbox(userId, status, pageable).map(mapper::toDto);
    }

    @Operation(summary = "Список приглашений по чату (для админов/владельца)")
    @GetMapping("/{chatId}/join-requests")
    public Page<ChatJoinRequestResponse> listByChat(
            @PathVariable UUID chatId,
            @RequestParam(required = false) String status,
            Pageable pageable,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID actorId = UUID.fromString(jwt.getSubject());
        return chatJoinRequestService.listByChat(chatId, actorId, status, pageable).map(mapper::toDto);
    }

    @Operation(summary = "Принять приглашение")
    @PostMapping("/join-requests/{requestId}/accept")
    public ChatJoinRequestResponse accept(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return mapper.toDto(chatJoinRequestService.accept(requestId, userId));
    }

    @Operation(summary = "Отклонить приглашение")
    @PostMapping("/join-requests/{requestId}/reject")
    public ChatJoinRequestResponse reject(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return mapper.toDto(chatJoinRequestService.reject(requestId, userId));
    }
}
