package tech.sh2rman.coreservice.domain.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sh2rman.coreservice.domain.user.dto.UpdateMyProfileRequest;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;
import tech.sh2rman.coreservice.domain.user.exception.UserProfileNotFoundException;
import tech.sh2rman.coreservice.domain.user.repository.UserProfileRepository;
import tech.sh2rman.coreservice.domain.user.service.UserProfileService;
import tech.sh2rman.coreservice.integration.keycloak.KeycloakUserEvent;
import tech.sh2rman.coreservice.integration.minio.MinioStorageService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final MinioStorageService storage;

    @Override
    @Transactional
    public void createIfAbsent(KeycloakUserEvent event) {
        UUID userId = UUID.fromString(event.getUserId());

        if (userProfileRepository.existsById(userId)) {
            return;
        }

        UserProfileEntity profile = UserProfileEntity.builder()
                .id(userId)
                .email(event.getDetails() != null ? event.getDetails().get("email") : null)
                .build();

        userProfileRepository.save(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileEntity> search(String q, UUID meUserId, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return Page.empty(pageable);
        }

        return userProfileRepository.searchActive(q.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileEntity getMe(UUID meUserId) {
        return userProfileRepository.findById(meUserId)
                .orElseThrow(UserProfileNotFoundException::new);
    }

    @Override
    @Transactional
    public UserProfileEntity updateMe(UUID meUserId, UpdateMyProfileRequest req) {
        UserProfileEntity u = userProfileRepository.findById(meUserId)
                .orElseThrow(UserProfileNotFoundException::new);

        if (req.username() != null) {
            u.setUsername(normalizePatchString(req.username()));
        }

        if (req.displayName() != null) {
            u.setDisplayName(normalizePatchString(req.displayName()));
        }

        if (req.bio() != null) {
            u.setBio(normalizePatchString(req.bio()));
        }

        if (req.isProfilePublic() != null) {
            u.setIsProfilePublic(req.isProfilePublic());
        }

        if (req.coverFile() != null && !req.coverFile().isEmpty()) {

            long nextVersion = (u.getAvatarVersion() == null ? 1L : u.getAvatarVersion() + 1L);
            String versionStr = String.valueOf(nextVersion);

            String objectKey = storage.putAvatar(meUserId, req.coverFile(), versionStr);

            u.setAvatarObjectKey(objectKey);

            u.setAvatarUrl(null);

            u.setAvatarVersion(nextVersion);
        }

        return userProfileRepository.save(u);
    }

    private String normalizePatchString(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

}
