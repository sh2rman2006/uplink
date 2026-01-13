package tech.sh2rman.coreservice.domain.chat.service;

import tech.sh2rman.coreservice.domain.chat.entity.Chat;
import tech.sh2rman.coreservice.domain.chat.entity.ChatParticipant;
import tech.sh2rman.coreservice.domain.chat.entity.Message;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;

import java.util.UUID;

public interface ChatAccessService {
    Chat requireChat(UUID chatId);

    ChatParticipant requireParticipant(UUID chatId, UUID userId);

    Message requireMessage(UUID chatId, UUID messageId);

    void assertCanRead(ChatParticipant me);

    void assertCanSend(Chat chat, ChatParticipant me);

    void assertCanEditText(Chat chat, ChatParticipant me, Message message, UUID userId);

    void assertCanDelete(Chat chat, ChatParticipant me, Message message, UUID userId);

    UserProfileEntity requireUser(UUID userId);

    void assertCanChangeChatSettings(Chat chat, ChatParticipant me);

    void assertCanChangeChatInfo(Chat chat, ChatParticipant me);

    void assertCanSendMedia(Chat chat, ChatParticipant me);
}
