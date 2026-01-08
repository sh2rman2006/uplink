package tech.sh2rman.coreservice.domain.chat.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateChatResponse {
    private UUID chatId;

    public CreateChatResponse(UUID chatId) {
        this.chatId = chatId;
    }
}
