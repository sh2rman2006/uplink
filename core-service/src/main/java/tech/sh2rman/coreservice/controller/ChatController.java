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
import tech.sh2rman.coreservice.domain.chat.dto.ChatListItemResponse;
import tech.sh2rman.coreservice.domain.chat.dto.CreateChatRequest;
import tech.sh2rman.coreservice.domain.chat.dto.CreateChatResponse;
import tech.sh2rman.coreservice.domain.chat.service.ChatService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "API чатов")
public class ChatController {
    private final ChatService chatService;

    @Operation(summary = "Создать чат")
    @PostMapping("/create")
    public CreateChatResponse createChat(
            @Valid @RequestBody CreateChatRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        UUID chatId = chatService.createChat(userId, req).getId();
        return new CreateChatResponse(chatId);
    }

    @Operation(summary = "Список моих чатов (unread + lastMessage preview)")
    @GetMapping
    public Page<ChatListItemResponse> listMyChats(
            Pageable pageable,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return chatService.listMyChats(userId, pageable);
    }

    @Operation(summary = "Поиск по моим чатам (title/description)")
    @GetMapping("/search")
    public Page<ChatListItemResponse> searchMyChats(
            @RequestParam String q,
            Pageable pageable,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return chatService.searchMyChats(userId, q, pageable);
    }
}
