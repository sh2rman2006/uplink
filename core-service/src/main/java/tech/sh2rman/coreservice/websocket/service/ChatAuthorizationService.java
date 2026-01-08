package tech.sh2rman.coreservice.websocket.service;

import java.util.UUID;

public interface ChatAuthorizationService {
    void assertCanSubscribe(UUID chatId, String keycloakSub);

    void assertCanSend(UUID chatId, String keycloakSub);
}
