package tech.sh2rman.coreservice.domain.chat.mapper;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.sh2rman.coreservice.domain.chat.dto.res.MessageDto;
import tech.sh2rman.coreservice.domain.chat.entity.Attachment;
import tech.sh2rman.coreservice.domain.chat.entity.Message;
import tech.sh2rman.coreservice.domain.common.mapper.EntitiesMapper;
import tech.sh2rman.coreservice.integration.minio.MinioStorageService;

import java.util.Comparator;
import java.util.List;


@Component
@RequiredArgsConstructor
public class MessageMapper implements EntitiesMapper<Message, MessageDto> {

    private final MinioStorageService storage;

    @Override
    public MessageDto toDto(@NotNull Message m) {
        MessageDto dto = new MessageDto();

        dto.setId(m.getId());
        dto.setChatId(m.getChat().getId());
        dto.setSenderId(m.getSender().getId());

        dto.setType(m.getType());
        dto.setText(m.getText());

        dto.setReplyToMessageId(
                m.getReplyToMessage() != null ? m.getReplyToMessage().getId() : null
        );

        dto.setCreatedAt(m.getCreatedAt());

        dto.setUpdatedAt(m.getUpdatedAt());
        dto.setEditCount(m.getEditCount());
        dto.setLastEditedAt(m.getLastEditedAt());

        dto.setAttachments(mapAttachments(m));

        return dto;
    }

    private List<MessageDto.AttachmentDto> mapAttachments(Message m) {
        if (m.getAttachments() == null || m.getAttachments().isEmpty()) {
            return List.of();
        }

        return m.getAttachments().stream()
                .sorted(Comparator.comparing(Attachment::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toAttachmentDto)
                .toList();
    }

    private MessageDto.AttachmentDto toAttachmentDto(Attachment a) {
        MessageDto.AttachmentDto dto = new MessageDto.AttachmentDto();

        dto.setId(a.getId());
        dto.setType(a.getType() != null ? a.getType().name() : null);

        dto.setMimeType(safeTrim(a.getMimeType()));
        dto.setFileName(safeTrim(a.getFileName()));
        dto.setFileSize(a.getFileSize());

        String objectKey = safeTrim(a.getObjectKey());
        if (objectKey != null) {
            dto.setUrl(storage.presignGet(objectKey));
        } else {
            dto.setUrl(safeTrim(a.getUrl()));
        }

        String thumb = safeTrim(a.getThumbnailUrl());
        if (thumb != null) {
            dto.setThumbnailUrl(thumb);
        } else {
            dto.setThumbnailUrl(safeTrim(a.getPreviewUrl()));
        }

        return dto;
    }

    private String safeTrim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @Override
    public Message toEntity(@NotNull MessageDto dto) {
        throw new UnsupportedOperationException("Mapping MessageDto -> Message is not supported");
    }
}