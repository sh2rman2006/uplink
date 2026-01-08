package tech.sh2rman.coreservice.domain.chat.dto;

import lombok.Getter;
import lombok.Setter;

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

    private String type; // MessageType.name()

    private String text;

    private UUID replyToMessageId;

    private List<AttachmentDto> attachments = new ArrayList<>();

    private OffsetDateTime createdAt;


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
