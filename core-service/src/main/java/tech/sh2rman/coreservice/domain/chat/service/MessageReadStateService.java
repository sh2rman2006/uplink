package tech.sh2rman.coreservice.domain.chat.service;

import tech.sh2rman.coreservice.domain.chat.dto.res.MessageReadStateDto;

import java.util.UUID;

public interface MessageReadStateService {
    MessageReadStateDto getMyReadState(UUID chatId, UUID userId);
}
