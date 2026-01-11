package tech.sh2rman.coreservice.domain.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sh2rman.coreservice.domain.user.dto.MyProfileResponse;
import tech.sh2rman.coreservice.domain.user.dto.UpdateMyProfileRequest;
import tech.sh2rman.coreservice.domain.user.dto.UserProfileSearchItemResponse;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;
import tech.sh2rman.coreservice.domain.user.exception.UserProfileNotFoundException;
import tech.sh2rman.coreservice.domain.user.repository.UserProfileRepository;
import tech.sh2rman.coreservice.domain.user.service.UserProfileService;
import tech.sh2rman.coreservice.integration.keycloak.KeycloakUserEvent;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    private final UserProfileRepository userProfileRepository;

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
    public Page<UserProfileSearchItemResponse> search(String q, UUID meUserId, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return Page.empty(pageable);
        }

        return userProfileRepository.searchActive(q.trim(), pageable)
                .map(u -> toSearchItem(u, meUserId));
    }

    private UserProfileSearchItemResponse toSearchItem(UserProfileEntity u, UUID meUserId) {
        UserProfileSearchItemResponse r = new UserProfileSearchItemResponse();
        r.setId(u.getId());

        boolean isMe = u.getId() != null && u.getId().equals(meUserId);
        boolean isPublic = Boolean.TRUE.equals(u.getIsProfilePublic());

        r.setUsername(isPublic || isMe ? u.getUsername() : null);
        r.setDisplayName(isPublic || isMe ? u.getDisplayName() : null);
        r.setAvatarUrl(isPublic || isMe ? u.getAvatarUrl() : null);
        r.setAvatarVersion(isPublic || isMe ? u.getAvatarVersion() : null);
        r.setIsProfilePublic(u.getIsProfilePublic());

        return r;
    }

    @Override
    @Transactional(readOnly = true)
    public MyProfileResponse getMe(UUID meUserId) {
        UserProfileEntity u = userProfileRepository.findById(meUserId)
                .orElseThrow(UserProfileNotFoundException::new);
        return toMe(u);
    }

    @Override
    @Transactional
    public MyProfileResponse updateMe(UUID meUserId, UpdateMyProfileRequest req) {
        UserProfileEntity u = userProfileRepository.findById(meUserId)
                .orElseThrow(UserProfileNotFoundException::new);

        if (req.username() != null) u.setUsername(req.username().trim());
        if (req.displayName() != null) u.setDisplayName(req.displayName().trim());
        if (req.bio() != null) u.setBio(req.bio().trim());
        if (req.isProfilePublic() != null) u.setIsProfilePublic(req.isProfilePublic());

        return toMe(userProfileRepository.save(u));
    }

    private MyProfileResponse toMe(UserProfileEntity u) {
        MyProfileResponse r = new MyProfileResponse();
        r.setId(u.getId());
        r.setUsername(u.getUsername());
        r.setEmail(u.getEmail());
        r.setDisplayName(u.getDisplayName());
        r.setBio(u.getBio());
        r.setAvatarUrl(u.getAvatarUrl());
        r.setAvatarVersion(u.getAvatarVersion());
        r.setIsProfilePublic(u.getIsProfilePublic());
        r.setStatus(u.getStatus());
        r.setCreatedAt(u.getCreatedAt());
        r.setUpdatedAt(u.getUpdatedAt());
        return r;
    }
}
