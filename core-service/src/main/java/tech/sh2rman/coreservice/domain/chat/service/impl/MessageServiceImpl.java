package tech.sh2rman.coreservice.domain.chat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sh2rman.coreservice.domain.chat.dto.CreateTextMessageRequest;
import tech.sh2rman.coreservice.domain.chat.dto.MessageDto;
import tech.sh2rman.coreservice.domain.chat.entity.Chat;
import tech.sh2rman.coreservice.domain.chat.entity.Message;
import tech.sh2rman.coreservice.domain.chat.exception.ChatNotFoundException;
import tech.sh2rman.coreservice.domain.chat.exception.MessageBadRequestException;
import tech.sh2rman.coreservice.domain.chat.mapper.MessageMapper;
import tech.sh2rman.coreservice.domain.chat.model.MessageType;
import tech.sh2rman.coreservice.domain.chat.repository.ChatRepository;
import tech.sh2rman.coreservice.domain.chat.repository.MessageRepository;
import tech.sh2rman.coreservice.domain.chat.service.MessageService;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;
import tech.sh2rman.coreservice.domain.user.repository.UserProfileRepository;
import tech.sh2rman.coreservice.websocket.dto.WsEvent;
import tech.sh2rman.coreservice.websocket.dto.WsEventType;
import tech.sh2rman.coreservice.websocket.service.ChatAuthorizationService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserProfileRepository userProfileRepository;
    private final ChatAuthorizationService chatAuthorizationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageMapper messageMapper;

    @Override
    @Transactional
    public MessageDto sendText(UUID chatId, UUID userId, CreateTextMessageRequest req) {

        chatAuthorizationService.assertCanSend(chatId, userId.toString());

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException(chatId));

        UserProfileEntity sender = userProfileRepository.findById(userId)
                .orElseThrow(() -> new MessageBadRequestException("User profile not found"));

        OffsetDateTime now = OffsetDateTime.now();

        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setType(MessageType.TEXT);
        message.setText(req.text());
        message.setIsForwarded(false);
        message.setCreatedAt(now);
        message.setUpdatedAt(now);

        Message saved = messageRepository.save(message);

        chat.setLastMessageAt(now);
        chat.setLastMessageId(saved.getId());
        chatRepository.save(chat);

        MessageDto dto = messageMapper.toDto(saved);

        messagingTemplate.convertAndSend(
                "/topic/chat." + chatId,
                WsEvent.of(WsEventType.MESSAGE_CREATED, dto)
        );

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageDto> list(UUID chatId, UUID userId, OffsetDateTime before) {
        chatAuthorizationService.assertCanSubscribe(chatId, userId.toString());

        List<Message> items = (before == null)
                ? messageRepository.findTop50ByChatIdAndDeletedAtIsNullOrderByCreatedAtDesc(chatId)
                : messageRepository.findTop50ByChatIdAndDeletedAtIsNullAndCreatedAtLessThanOrderByCreatedAtDesc(chatId, before);

        return messageMapper.toDtoList(items);
    }

}
