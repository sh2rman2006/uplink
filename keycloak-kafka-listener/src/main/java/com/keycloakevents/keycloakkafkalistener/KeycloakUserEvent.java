package com.keycloakevents.keycloakkafkalistener;

import java.util.HashMap;
import java.util.Map;

public class KeycloakUserEvent {
    public String kind;            // "EVENT" | "ADMIN_EVENT"
    public String eventType;        // REGISTER / LOGIN / ...
    public long timestamp;          // millis
    public String realmId;
    public String realmName;

    public String userId;
    public String clientId;
    public String ipAddress;
    public String sessionId;

    public Map<String, String> details = new HashMap<>();

    // admin-specific
    public String operationType;
    public String resourceType;
    public String resourcePath;
    public String authClientId;
    public String authUserId;
    public String authIpAddress;
    public String error;

    public KeycloakUserEvent() {}
}
