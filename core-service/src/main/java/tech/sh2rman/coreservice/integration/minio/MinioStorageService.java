package tech.sh2rman.coreservice.integration.minio;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.sh2rman.coreservice.config.properties.MinioProperties;

import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

@Service
public class MinioStorageService {

    private final MinioClient minio;
    @Qualifier("minio-tech.sh2rman.coreservice.config.properties.MinioProperties")
    private final MinioProperties props;

    public MinioStorageService(MinioClient minio,
                               @Qualifier("minio-tech.sh2rman.coreservice.config.properties.MinioProperties")
                               MinioProperties props) {
        this.minio = minio;
        this.props = props;
    }

    private final static Set<String> imageMimeTypes = Set.of(
            "image/png",
            "image/jpeg",
            "image/jpg",
            "image/gif",
            "image/webp"
    );

    public String putAvatar(UUID userId, MultipartFile file, String version) {
        String objectName = props.getAvatarsPrefix() + "/" + userId + "/" + version;

        return uploadImage(file, objectName);
    }

    public String getUserAvatarObjectUrl(UUID userId, String version) {
        return presignGet(props.getAvatarsPrefix() + "/" + userId + "/" + version);
    }

    public String putChatAvatar(UUID chatId, MultipartFile file) {
        String objectName = props.getChatAvatarsPrefix() + "/" + chatId;

        return uploadImage(file, objectName);
    }

    public String getChatAvatarObjectUrl(UUID chatId) {
        return presignGet(props.getChatAvatarsPrefix() + "/" + chatId);
    }

    private String uploadImage(MultipartFile file, String objectName) {
        try (InputStream in = file.getInputStream()) {
            if (file.isEmpty() || !imageMimeTypes.contains(file.getContentType())) {
                throw new IllegalArgumentException("Invalid avatar file");
            }
            minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(props.getBucket())
                            .object(objectName)
                            .stream(in, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return objectName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image to MinIO", e);
        }
    }

    public String presignGet(String objectName) {
        if (objectName == null) return null;
        try {
            return minio.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(props.getBucket())
                            .object(objectName)
                            .expiry(props.getPresignTtlSeconds())
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to presign MinIO url", e);
        }
    }
}

