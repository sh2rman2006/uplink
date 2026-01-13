package tech.sh2rman.coreservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import tech.sh2rman.coreservice.domain.chat.dto.req.CreateChatRequest;
import tech.sh2rman.coreservice.domain.chat.dto.req.EditChatRequest;
import tech.sh2rman.coreservice.domain.chat.dto.res.ChatDto;
import tech.sh2rman.coreservice.domain.chat.dto.res.ChatListItemResponse;
import tech.sh2rman.coreservice.domain.chat.mapper.ChatMapper;
import tech.sh2rman.coreservice.domain.chat.service.ChatService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "API чатов")
public class ChatController {
    private final ChatService chatService;
    private final ChatMapper chatMapper;

    @Operation(summary = "Создать чат")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ChatDto createChat(
            @Valid @ModelAttribute CreateChatRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return chatMapper.toDto(chatService.createChat(userId, req));
    }

    @Operation(summary = "Изменить чат")
    @PatchMapping(value = "/{chatId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ChatDto editChat(@Valid @ModelAttribute EditChatRequest req,
                            @AuthenticationPrincipal Jwt jwt,
                            @PathVariable UUID chatId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return chatMapper.toDto(chatService.editChat(userId, chatId, req));
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
