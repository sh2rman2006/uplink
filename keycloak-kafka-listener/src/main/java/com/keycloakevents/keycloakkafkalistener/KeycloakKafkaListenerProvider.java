package com.keycloakevents.keycloakkafkalistener;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.util.JsonSerialization;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

final class KeycloakKafkaListenerProvider implements EventListenerProvider {

    private static final Logger log = Logger.getLogger(KeycloakKafkaListenerProvider.class.getName());

    private final KeycloakSession session;
    private final KafkaProducer<String, String> producer;

    private final String topicUser;
    private final String topicAdmin;

    private final boolean enabled;
    private final boolean adminEventsEnabled;

    private final Set<String> enabledEventsUpper;
    private final boolean includeDetails;

    KeycloakKafkaListenerProvider(
            KeycloakSession session,
            KafkaProducer<String, String> producer,
            String topicUser,
            String topicAdmin,
            boolean enabled,
            boolean adminEventsEnabled,
            Set<String> enabledEventsUpper,
            boolean includeDetails
    ) {
        this.session = session;
        this.producer = producer;
        this.topicUser = topicUser;
        this.topicAdmin = topicAdmin;
        this.enabled = enabled;
        this.adminEventsEnabled = adminEventsEnabled;
        this.enabledEventsUpper = enabledEventsUpper;
        this.includeDetails = includeDetails;
    }

    @Override
    public void onEvent(Event event) {
        if (!enabled) return;
        if (event == null) return;

        String type = safe(event.getType() != null ? event.getType().name() : null).toUpperCase();
        if (!enabledEventsUpper.isEmpty() && !enabledEventsUpper.contains(type)) {
            return;
        }

        RealmModel realm = session.getContext().getRealm();

        KeycloakUserEvent payload = new KeycloakUserEvent();
        payload.kind = "EVENT";
        payload.eventType = type;
        payload.timestamp = event.getTime(); // в Keycloak это long (не null)
        payload.realmId = safe(realm != null ? realm.getId() : null);
        payload.realmName = safe(realm != null ? realm.getName() : null);

        payload.userId = safe(event.getUserId());
        payload.clientId = safe(event.getClientId());
        payload.ipAddress = safe(event.getIpAddress());
        payload.sessionId = safe(event.getSessionId());

        if (includeDetails) {
            Map<String, String> details = event.getDetails();
            if (details != null) payload.details.putAll(details);
        }

        send(topicUser, payload.userId, payload);
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        if (!enabled) return;
        if (!adminEventsEnabled) return;
        if (adminEvent == null) return;

        RealmModel realm = session.getContext().getRealm();

        KeycloakUserEvent payload = new KeycloakUserEvent();
        payload.kind = "ADMIN_EVENT";
        payload.timestamp = System.currentTimeMillis();

        payload.realmId = safe(realm != null ? realm.getId() : null);
        payload.realmName = safe(realm != null ? realm.getName() : null);

        payload.operationType = safe(adminEvent.getOperationType() != null ? adminEvent.getOperationType().name() : null);
        payload.resourceType = safe(adminEvent.getResourceType() != null ? adminEvent.getResourceType().name() : null);
        payload.resourcePath = safe(adminEvent.getResourcePath());
        payload.error = safe(adminEvent.getError());

        if (adminEvent.getAuthDetails() != null) {
            payload.authClientId = safe(adminEvent.getAuthDetails().getClientId());
            payload.authUserId = safe(adminEvent.getAuthDetails().getUserId());
            payload.authIpAddress = safe(adminEvent.getAuthDetails().getIpAddress());
        }

        // ключ сообщения — userId админки, либо clientId, либо realm
        String key = payload.authUserId;
        if (key == null || key.isBlank()) key = payload.authClientId;
        if (key == null || key.isBlank()) key = payload.realmId;

        send(topicAdmin, key, payload);
    }

    private void send(String topic, String key, KeycloakUserEvent payload) {
        if (producer == null) return;
        if (topic == null || topic.isBlank()) return;

        try {
            byte[] bytes = JsonSerialization.writeValueAsBytes(payload);
            String json = new String(bytes, StandardCharsets.UTF_8);

            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, json);

            producer.send(record, (meta, ex) -> {
                if (ex != null) {
                    log.log(Level.WARNING, "Failed to send Keycloak event to Kafka topic=" + topic + " key=" + key, ex);
                }
            });

        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to serialize/send Keycloak event to Kafka topic=" + topic, e);
        }
    }

    @Override
    public void close() {
        // producer закрываем в Factory (shared instance), тут ничего
    }

    private static String safe(String v) {
        if (v == null) return "";
        return v;
    }
}
