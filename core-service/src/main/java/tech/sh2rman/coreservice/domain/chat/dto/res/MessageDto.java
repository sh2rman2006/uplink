package tech.sh2rman.coreservice.domain.chat.dto.res;

import lombok.Getter;
import lombok.Setter;
import tech.sh2rman.coreservice.domain.chat.model.MessageType;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class MessageDto {

    private UUID id;
    private UUID chatId;
    private UUID senderId;

    private MessageType type;

    private String text;

    private UUID replyToMessageId;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
    private Integer editCount;
    private OffsetDateTime lastEditedAt;

    private List<AttachmentDto> attachments = new ArrayList<>();

    @Getter
    @Setter
    public static class AttachmentDto {
        private UUID id;
        private String type;
        private String mimeType;
        private String fileName;
        private Long fileSize;
        private String url;
        private String thumbnailUrl;
    }
}
