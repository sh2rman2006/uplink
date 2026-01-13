package tech.sh2rman.coreservice.domain.user.mapper;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.sh2rman.coreservice.domain.common.mapper.EntitiesMapper;
import tech.sh2rman.coreservice.domain.user.dto.MyProfileResponse;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;
import tech.sh2rman.coreservice.integration.minio.MinioStorageService;

@Component
@RequiredArgsConstructor
public class UserProfileMapper implements EntitiesMapper<UserProfileEntity, MyProfileResponse> {

    private final MinioStorageService storage;

    @Override
    public MyProfileResponse toDto(@NotNull UserProfileEntity u) {
        MyProfileResponse r = new MyProfileResponse();

        r.setId(u.getId());
        r.setUsername(u.getUsername());
        r.setEmail(u.getEmail());

        r.setDisplayName(u.getDisplayName());
        r.setBio(u.getBio());

        r.setAvatarVersion(u.getAvatarVersion());
        r.setIsProfilePublic(u.getIsProfilePublic());
        r.setStatus(u.getStatus());

        r.setCreatedAt(u.getCreatedAt());
        r.setUpdatedAt(u.getUpdatedAt());

        String objectKey = safeTrim(u.getAvatarObjectKey());
        if (objectKey != null) {
            r.setAvatarUrl(storage.presignGet(objectKey));
        } else {
            r.setAvatarUrl(safeTrim(u.getAvatarUrl()));
        }

        return r;
    }

    @Override
    public UserProfileEntity toEntity(@NotNull MyProfileResponse dto) {
        throw new UnsupportedOperationException(
                "Mapping MyProfileResponse -> UserProfileEntity is not supported"
        );
    }

    private String safeTrim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
