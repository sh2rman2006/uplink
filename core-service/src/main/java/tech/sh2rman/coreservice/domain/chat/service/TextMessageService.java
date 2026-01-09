package tech.sh2rman.coreservice.domain.chat.service;

import tech.sh2rman.coreservice.domain.chat.dto.CreateTextMessageRequest;
import tech.sh2rman.coreservice.domain.chat.dto.EditTextMessageRequest;
import tech.sh2rman.coreservice.domain.chat.dto.MessageDto;

import java.util.UUID;

public interface TextMessageService {
    MessageDto sendText(UUID chatId, UUID userId, CreateTextMessageRequest req);

    MessageDto editText(UUID chatId, UUID userId, UUID messageId, EditTextMessageRequest req);
}
