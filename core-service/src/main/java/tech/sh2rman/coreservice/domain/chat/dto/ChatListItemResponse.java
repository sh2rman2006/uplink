package tech.sh2rman.coreservice.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.sh2rman.coreservice.domain.chat.model.ChatType;
import tech.sh2rman.coreservice.domain.chat.model.MessageType;

import java.time.OffsetDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ChatListItemResponse {

    private UUID chatId;
    private ChatType type;
    private String title;
    private String description;

    private OffsetDateTime updatedAt;

    private UUID lastMessageId;
    private OffsetDateTime lastMessageAt;

    private MessageType lastMessageType;
    private String lastMessageText;
    private UUID lastMessageSenderId;
    private OffsetDateTime lastMessageCreatedAt;

    private long unreadCount;
}
