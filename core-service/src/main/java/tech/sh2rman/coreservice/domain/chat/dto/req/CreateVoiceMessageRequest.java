package tech.sh2rman.coreservice.domain.chat.dto.req;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record CreateVoiceMessageRequest(
        MultipartFile file,
        UUID replyToMessageId
) {}

