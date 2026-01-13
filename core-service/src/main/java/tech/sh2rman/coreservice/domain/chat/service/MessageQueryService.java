package tech.sh2rman.coreservice.domain.chat.service;

import tech.sh2rman.coreservice.domain.chat.dto.res.MessageDto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface MessageQueryService {
    List<MessageDto> list(UUID chatId, UUID userId, OffsetDateTime before);
}
