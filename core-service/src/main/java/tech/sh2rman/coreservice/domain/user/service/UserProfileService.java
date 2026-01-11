package tech.sh2rman.coreservice.domain.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tech.sh2rman.coreservice.domain.user.dto.MyProfileResponse;
import tech.sh2rman.coreservice.domain.user.dto.UpdateMyProfileRequest;
import tech.sh2rman.coreservice.domain.user.dto.UserProfileSearchItemResponse;
import tech.sh2rman.coreservice.integration.keycloak.KeycloakUserEvent;

import java.util.UUID;

public interface UserProfileService {

    void createIfAbsent(KeycloakUserEvent event);

    Page<UserProfileSearchItemResponse> search(String q, UUID meUserId, Pageable pageable);

    MyProfileResponse getMe(UUID meUserId);

    MyProfileResponse updateMe(UUID meUserId, UpdateMyProfileRequest req);
}
