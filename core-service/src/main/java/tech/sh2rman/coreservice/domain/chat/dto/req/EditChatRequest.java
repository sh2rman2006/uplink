package tech.sh2rman.coreservice.domain.chat.dto.req;

import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

public record EditChatRequest(
        @Size(max = 128) String title,
        @Size(max = 2048) String description,
        MultipartFile coverFile,

        Boolean allowSendMedia,
        Boolean allowAddUsers,
        Boolean allowPinMessages,
        Boolean allowChangeInfo
) implements Serializable {}

