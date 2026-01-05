package tech.sh2rman.coreservice.integration.keycloak;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.sh2rman.coreservice.service.UserProfileService;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakUserEventsConsumer {

    private final UserProfileService userProfileService;

    @KafkaListener(
            topics = "${uplink.kafka.topics.keycloak-user-events}",
            containerFactory = "keycloakKafkaListenerContainerFactory"
    )
    public void onMessage(KeycloakUserEvent event) {

        if (!"REGISTER".equals(event.getEventType())) {
            return;
        }
        log.info("Received Keycloak event: type={}, userId={}",
                event.getEventType(), event.getUserId());
        userProfileService.createIfAbsent(event);
    }
}