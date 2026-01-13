package tech.sh2rman.coreservice.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "minio")
@Component
public class MinioProperties {
    private String url;
    private String accessKey;
    private String secretKey;
    private String bucket;

    private int presignTtlSeconds;

    private String avatarsPrefix;
    private String chatAvatarsPrefix;
    private String attachmentsImagePrefix;
    private String attachmentsFilePrefix;
    private String attachmentsVideoPrefix;
    private String attachmentsAudioPrefix;
}
