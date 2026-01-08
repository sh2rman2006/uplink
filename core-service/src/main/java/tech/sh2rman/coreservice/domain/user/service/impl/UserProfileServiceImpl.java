package tech.sh2rman.coreservice.domain.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;
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
}
