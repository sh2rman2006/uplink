package tech.sh2rman.coreservice.domain.user.mapper;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.sh2rman.coreservice.domain.common.mapper.EntitiesMapper;
import tech.sh2rman.coreservice.domain.user.dto.UserProfileSearchItemResponse;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;
import tech.sh2rman.coreservice.integration.minio.MinioStorageService;

@Component
@RequiredArgsConstructor
public class UserProfileSearchItemMapper implements EntitiesMapper<UserProfileEntity, UserProfileSearchItemResponse> {

    private final MinioStorageService storage;

    @Override
    public UserProfileSearchItemResponse toDto(@NotNull UserProfileEntity u) {
        UserProfileSearchItemResponse r = new UserProfileSearchItemResponse();
        r.setId(u.getId());
        r.setUsername(u.getUsername());
        r.setDisplayName(u.getDisplayName());

        String objectKey = safeTrim(u.getAvatarObjectKey());
        if (objectKey != null) r.setAvatarUrl(storage.presignGet(objectKey));
        else r.setAvatarUrl(safeTrim(u.getAvatarUrl()));

        r.setAvatarVersion(u.getAvatarVersion());
        r.setIsProfilePublic(u.getIsProfilePublic());
        return r;
    }

    @Override
    public UserProfileEntity toEntity(@NotNull UserProfileSearchItemResponse dto) {
        throw new UnsupportedOperationException(
                "Mapping UserProfileSearchItemResponse -> UserProfileEntity is not supported"
        );
    }

    private String safeTrim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}

