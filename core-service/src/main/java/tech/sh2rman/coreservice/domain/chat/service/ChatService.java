package tech.sh2rman.coreservice.domain.chat.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tech.sh2rman.coreservice.domain.chat.dto.ChatListItemResponse;
import tech.sh2rman.coreservice.domain.chat.dto.CreateChatRequest;
import tech.sh2rman.coreservice.domain.chat.entity.Chat;

import java.util.UUID;

public interface ChatService {

    Chat createChat(UUID creatorUserId, CreateChatRequest req);

    Page<ChatListItemResponse> listMyChats(UUID userId, Pageable pageable);

    Page<ChatListItemResponse> searchMyChats(UUID userId, String q, Pageable pageable);
}
