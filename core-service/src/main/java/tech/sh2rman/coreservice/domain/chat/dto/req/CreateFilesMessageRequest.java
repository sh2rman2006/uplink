package tech.sh2rman.coreservice.domain.chat.dto.req;

import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public record CreateFilesMessageRequest(
        List<MultipartFile> files,
        @Size(max = 4096) String text,
        UUID replyToMessageId
) {}
