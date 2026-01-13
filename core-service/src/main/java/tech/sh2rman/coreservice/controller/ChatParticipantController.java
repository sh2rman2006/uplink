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
import tech.sh2rman.coreservice.domain.chat.dto.req.AddChatParticipantRequest;
import tech.sh2rman.coreservice.domain.chat.dto.req.ChangeChatParticipantRoleRequest;
import tech.sh2rman.coreservice.domain.chat.dto.res.ChatParticipantResponse;
import tech.sh2rman.coreservice.domain.chat.mapper.ChatParticipantMapper;
import tech.sh2rman.coreservice.domain.chat.service.ChatParticipantService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "Chat Participant")
public class ChatParticipantController {

    private final ChatParticipantService chatParticipantService;
    private final ChatParticipantMapper chatParticipantMapper;

    @PostMapping("/{chatId}/participants")
    public void add(
            @PathVariable UUID chatId,
            @Valid @RequestBody AddChatParticipantRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID actorId = UUID.fromString(jwt.getSubject());
        chatParticipantService.add(chatId, actorId, req.userId(), req.role());
    }

    @DeleteMapping("/{chatId}/participants/{userId}")
    public void kick(
            @PathVariable UUID chatId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID actorId = UUID.fromString(jwt.getSubject());
        chatParticipantService.kick(chatId, actorId, userId);
    }

    @PostMapping("/{chatId}/participants/me:leave")
    public void leave(
            @PathVariable UUID chatId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID actorId = UUID.fromString(jwt.getSubject());
        chatParticipantService.leave(chatId, actorId);
    }

    @PatchMapping("/{chatId}/participants/{userId}/role")
    public void changeRole(
            @PathVariable UUID chatId,
            @PathVariable UUID userId,
            @Valid @RequestBody ChangeChatParticipantRoleRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID actorId = UUID.fromString(jwt.getSubject());
        chatParticipantService.changeRole(chatId, actorId, userId, req.role());
    }

    @Operation(summary = "Список участников чата")
    @GetMapping("/{chatId}/participants")
    public Page<ChatParticipantResponse> listParticipants(
            @PathVariable UUID chatId,
            Pageable pageable,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return chatParticipantService.listParticipants(chatId, userId, pageable).map(chatParticipantMapper::toDto);
    }
}
