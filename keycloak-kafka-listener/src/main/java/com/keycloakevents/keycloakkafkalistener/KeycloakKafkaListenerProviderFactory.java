package com.keycloakevents.keycloakkafkalistener;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KeycloakKafkaListenerProviderFactory implements EventListenerProviderFactory {

    private static final Logger log = Logger.getLogger(KeycloakKafkaListenerProviderFactory.class.getName());

    // id провайдера, его будешь указывать в Keycloak как events listener
    public static final String PROVIDER_ID = "kafka";

    private final AtomicReference<KafkaProducer<String, String>> sharedProducer = new AtomicReference<>();

    // cached config
    private volatile boolean enabled;
    private volatile boolean adminEventsEnabled;
    private volatile boolean includeDetails;

    private volatile String bootstrapServers;
    private volatile String topicUser;
    private volatile String topicAdmin;

    private volatile Set<String> enabledEventsUpper;

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        KafkaProducer<String, String> producer = ensureProducer();

        return new KeycloakKafkaListenerProvider(
                session,
                producer,
                topicUser,
                topicAdmin,
                enabled,
                adminEventsEnabled,
                enabledEventsUpper,
                includeDetails
        );
    }

    @Override
    public void init(Config.Scope config) {
        KafkaTopicConfig.SafeConfig c = config::get;

        // включение
        enabled = KafkaTopicConfig.getBool(c, "enabled", true);
        adminEventsEnabled = KafkaTopicConfig.getBool(c, "adminEventsEnabled", true);
        includeDetails = KafkaTopicConfig.getBool(c, "includeDetails", true);

        // Kafka
        bootstrapServers = KafkaTopicConfig.get(c, "bootstrapServers",
                System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092"));

        topicUser = KafkaTopicConfig.get(c, "topicUser",
                System.getenv().getOrDefault("KAFKA_TOPIC_USER", "keycloak.user-events"));

        topicAdmin = KafkaTopicConfig.get(c, "topicAdmin",
                System.getenv().getOrDefault("KAFKA_TOPIC_ADMIN", "keycloak.admin-events"));

        // фильтр ивентов (если пусто — шлём всё)
        String enabledEvents = KafkaTopicConfig.get(c, "enabledEvents", System.getenv("KAFKA_ENABLED_EVENTS"));
        enabledEventsUpper = KafkaTopicConfig.parseUpperCsv(enabledEvents);

        log.info("Keycloak Kafka listener init: enabled=" + enabled
                + ", bootstrapServers=" + bootstrapServers
                + ", topicUser=" + topicUser
                + ", topicAdmin=" + topicAdmin
                + ", enabledEvents=" + (enabledEventsUpper.isEmpty() ? "<ALL>" : enabledEventsUpper));
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // не требуется
    }

    @Override
    public void close() {
        KafkaProducer<String, String> p = sharedProducer.getAndSet(null);
        if (p != null) {
            try {
                p.flush();
            } catch (Exception ignored) {}
            try {
                p.close();
            } catch (Exception ignored) {}
        }
    }

    private KafkaProducer<String, String> ensureProducer() {
        KafkaProducer<String, String> existing = sharedProducer.get();
        if (existing != null) return existing;

        if (!enabled) return null;

        KafkaProducer<String, String> created = null;
        try {
            Properties props = new Properties();

            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

            // разумные дефолты (можно переопределять env’ами)
            props.put(ProducerConfig.ACKS_CONFIG, System.getenv().getOrDefault("KAFKA_ACKS", "all"));
            props.put(ProducerConfig.RETRIES_CONFIG, System.getenv().getOrDefault("KAFKA_RETRIES", "5"));
            props.put(ProducerConfig.LINGER_MS_CONFIG, System.getenv().getOrDefault("KAFKA_LINGER_MS", "5"));
            props.put(ProducerConfig.BATCH_SIZE_CONFIG, System.getenv().getOrDefault("KAFKA_BATCH_SIZE", "32768"));
            props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, System.getenv().getOrDefault("KAFKA_COMPRESSION", "lz4"));
            props.put(ProducerConfig.CLIENT_ID_CONFIG, System.getenv().getOrDefault("KAFKA_CLIENT_ID", "keycloak-kafka-listener"));

            created = new KafkaProducer<>(props);

        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to create KafkaProducer for Keycloak listener", e);
            created = null;
        }

        if (created != null) {
            if (sharedProducer.compareAndSet(null, created)) {
                return created;
            } else {
                try { created.close(); } catch (Exception ignored) {}
                return sharedProducer.get();
            }
        }

        return null;
    }
}
