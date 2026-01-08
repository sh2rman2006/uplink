package tech.sh2rman.coreservice.domain.user.service;

import tech.sh2rman.coreservice.integration.keycloak.KeycloakUserEvent;

public interface UserProfileService {

    void createIfAbsent(KeycloakUserEvent event);
}
