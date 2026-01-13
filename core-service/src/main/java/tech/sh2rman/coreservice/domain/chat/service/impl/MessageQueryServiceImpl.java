package tech.sh2rman.coreservice.domain.chat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sh2rman.coreservice.domain.chat.dto.res.MessageDto;
import tech.sh2rman.coreservice.domain.chat.entity.Message;
import tech.sh2rman.coreservice.domain.chat.mapper.MessageMapper;
import tech.sh2rman.coreservice.domain.chat.repository.MessageRepository;
import tech.sh2rman.coreservice.domain.chat.service.ChatAccessService;
import tech.sh2rman.coreservice.domain.chat.service.MessageQueryService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageQueryServiceImpl implements MessageQueryService {

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final ChatAccessService access;

    @Override
    @Transactional(readOnly = true)
    public List<MessageDto> list(UUID chatId, UUID userId, OffsetDateTime before) {
        access.assertCanRead(access.requireParticipant(chatId, userId));

        List<Message> items = (before == null)
                ? messageRepository.findTop50ByChatIdAndDeletedAtIsNullOrderByCreatedAtDesc(chatId)
                : messageRepository.findTop50ByChatIdAndDeletedAtIsNullAndCreatedAtLessThanOrderByCreatedAtDesc(chatId, before);

        return messageMapper.toDtoList(items);
    }
}
