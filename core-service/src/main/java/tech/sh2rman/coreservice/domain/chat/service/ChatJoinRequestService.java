package tech.sh2rman.coreservice.domain.chat.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tech.sh2rman.coreservice.domain.chat.dto.ChatJoinRequestResponse;
import tech.sh2rman.coreservice.domain.chat.dto.InviteToChatRequest;

import java.util.UUID;

public interface ChatJoinRequestService {
    ChatJoinRequestResponse invite(UUID chatId, UUID actorUserId, InviteToChatRequest req);

    Page<ChatJoinRequestResponse> inbox(UUID userId, String status, Pageable pageable);

    Page<ChatJoinRequestResponse> listByChat(UUID chatId, UUID actorUserId, String status, Pageable pageable);

    ChatJoinRequestResponse accept(UUID requestId, UUID userId);

    ChatJoinRequestResponse reject(UUID requestId, UUID userId);
}
