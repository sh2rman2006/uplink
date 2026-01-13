package tech.sh2rman.coreservice.domain.chat.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tech.sh2rman.coreservice.domain.chat.dto.req.InviteToChatRequest;
import tech.sh2rman.coreservice.domain.chat.entity.ChatJoinRequest;

import java.util.UUID;

public interface ChatJoinRequestService {

    ChatJoinRequest invite(UUID chatId, UUID actorUserId, InviteToChatRequest req);

    Page<ChatJoinRequest> inbox(UUID userId, String status, Pageable pageable);

    Page<ChatJoinRequest> listByChat(UUID chatId, UUID actorUserId, String status, Pageable pageable);

    ChatJoinRequest accept(UUID requestId, UUID userId);

    ChatJoinRequest reject(UUID requestId, UUID userId);
}

