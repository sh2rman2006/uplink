package tech.sh2rman.coreservice.domain.chat.mapper;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import tech.sh2rman.coreservice.domain.chat.dto.res.MessageDto;
import tech.sh2rman.coreservice.domain.chat.entity.Message;
import tech.sh2rman.coreservice.domain.common.mapper.EntitiesMapper;

import java.util.List;

@Component
public class MessageMapper implements EntitiesMapper<Message, MessageDto> {

    @Override
    public MessageDto toDto(@NotNull Message m) {
        MessageDto dto = new MessageDto();

        dto.setId(m.getId());
        dto.setChatId(m.getChat().getId());
        dto.setSenderId(m.getSender().getId());

        dto.setType(m.getType());
        dto.setText(m.getText());

        dto.setReplyToMessageId(m.getReplyToMessage() != null ? m.getReplyToMessage().getId() : null);

        dto.setCreatedAt(m.getCreatedAt());

        dto.setUpdatedAt(m.getUpdatedAt());
        dto.setEditCount(m.getEditCount());
        dto.setLastEditedAt(m.getLastEditedAt());

        dto.setAttachments(List.of());
        return dto;
    }

    @Override
    public Message toEntity(@NotNull MessageDto dto) {
        throw new UnsupportedOperationException(
                "Mapping MessageDto -> Message is not supported"
        );
    }
}