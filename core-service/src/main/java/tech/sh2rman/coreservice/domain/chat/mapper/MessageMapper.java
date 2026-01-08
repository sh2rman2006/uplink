package tech.sh2rman.coreservice.domain.chat.mapper;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import tech.sh2rman.coreservice.domain.chat.dto.MessageDto;
import tech.sh2rman.coreservice.domain.chat.entity.Message;

@Component
public class MessageMapper implements EntitiesMapper<Message, MessageDto> {

    @Override
    public MessageDto toDto(@NotNull Message message) {
        MessageDto dto = new MessageDto();

        dto.setId(message.getId());
        dto.setChatId(message.getChat().getId());
        dto.setSenderId(message.getSender().getId());
        dto.setType(message.getType().name());

        dto.setText(message.getText());

        if (message.getReplyToMessage() != null) {
            dto.setReplyToMessageId(message.getReplyToMessage().getId());
        }

        dto.setCreatedAt(message.getCreatedAt());

        return dto;
    }

    @Override
    public Message toEntity(@NotNull MessageDto dto) {
        throw new UnsupportedOperationException(
                "Mapping MessageDto -> Message is not supported"
        );
    }
}