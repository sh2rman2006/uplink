package tech.sh2rman.coreservice.domain.chat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sh2rman.coreservice.domain.chat.dto.MessageReadStateDto;
import tech.sh2rman.coreservice.domain.chat.entity.ChatParticipant;
import tech.sh2rman.coreservice.domain.chat.repository.MessageRepository;
import tech.sh2rman.coreservice.domain.chat.service.MessageAccessService;
import tech.sh2rman.coreservice.domain.chat.service.MessageReadStateService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageReadStateServiceImpl implements MessageReadStateService {

    private final MessageAccessService access;
    private final MessageRepository messageRepository;

    @Override
    @Transactional(readOnly = true)
    public MessageReadStateDto getMyReadState(UUID chatId, UUID userId) {
        ChatParticipant me = access.requireParticipant(chatId, userId);
        access.assertCanRead(me);

        long unread = messageRepository.countUnread(chatId, userId, me.getLastReadAt());

        return new MessageReadStateDto(
                chatId,
                me.getLastReadMessageId(),
                me.getLastReadAt(),
                unread
        );
    }
}
