package tech.sh2rman.coreservice.domain.chat.service;

import tech.sh2rman.coreservice.domain.chat.dto.CreateChatRequest;
import tech.sh2rman.coreservice.domain.chat.entity.Chat;

import java.util.UUID;

public interface ChatService {

    Chat createChat(UUID creatorUserId, CreateChatRequest req);
}
