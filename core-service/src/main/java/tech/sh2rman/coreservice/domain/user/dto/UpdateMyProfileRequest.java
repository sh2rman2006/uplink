package tech.sh2rman.coreservice.domain.user.dto;

import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record UpdateMyProfileRequest(
        @Size(max = 32) String username,
        @Size(max = 64) String displayName,
        @Size(max = 280) String bio,
        Boolean isProfilePublic,
        MultipartFile coverFile
) {
}
