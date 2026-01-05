package tech.sh2rman.coreservice.integration.keycloak;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class KeycloakUserEvent {
    private String kind;            // "EVENT" | "ADMIN_EVENT"
    private String eventType;        // REGISTER / LOGIN / ...
    private long timestamp;          // millis
    private String realmId;
    private String realmName;

    private String userId;
    private String clientId;
    private String ipAddress;
    private String sessionId;

    private Map<String, String> details;

    // admin-specific
    private String operationType;
    private String resourceType;
    private String resourcePath;
    private String authClientId;
    private String authUserId;
    private String authIpAddress;
    private String error;
}