package tech.sh2rman.coreservice.integration.minio;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.sh2rman.coreservice.config.properties.MinioProperties;
import tech.sh2rman.coreservice.domain.chat.model.AttachmentType;
import tech.sh2rman.coreservice.integration.minio.model.AttachmentMime;
import tech.sh2rman.coreservice.integration.minio.model.UploadedObject;

import java.io.InputStream;
import java.util.UUID;

@Service
public class MinioStorageService {

    private final MinioClient minio;

    @Qualifier("minio-tech.sh2rman.coreservice.config.properties.MinioProperties")
    private final MinioProperties props;

    public MinioStorageService(
            MinioClient minio,
            @Qualifier("minio-tech.sh2rman.coreservice.config.properties.MinioProperties") MinioProperties props
    ) {
        this.minio = minio;
        this.props = props;
    }

    public String putAvatar(UUID userId, MultipartFile file, String version) {
        String objectName = props.getAvatarsPrefix() + "/" + userId + "/" + version;
        return uploadStrict(file, objectName, AttachmentMime.IMAGE, "avatar");
    }

    public String getUserAvatarObjectUrl(UUID userId, String version) {
        return presignGet(props.getAvatarsPrefix() + "/" + userId + "/" + version);
    }

    public String putChatAvatar(UUID chatId, MultipartFile file) {
        String objectName = props.getChatAvatarsPrefix() + "/" + chatId;
        return uploadStrict(file, objectName, AttachmentMime.IMAGE, "chat avatar");
    }

    public String getChatAvatarObjectUrl(UUID chatId) {
        return presignGet(props.getChatAvatarsPrefix() + "/" + chatId);
    }


    public String putAttachmentImage(UUID messageId, UUID chatId, MultipartFile file) {
        String objectName = buildAttachmentObjectName(props.getAttachmentsImagePrefix(), messageId, chatId, file);
        return uploadStrict(file, objectName, AttachmentMime.IMAGE, "image attachment");
    }

    public String putAttachmentVideo(UUID messageId, UUID chatId, MultipartFile file) {
        String objectName = buildAttachmentObjectName(props.getAttachmentsVideoPrefix(), messageId, chatId, file);
        return uploadStrict(file, objectName, AttachmentMime.VIDEO, "video attachment");
    }

    public String putAttachmentAudio(UUID messageId, UUID chatId, MultipartFile file) {
        String objectName = buildAttachmentObjectName(props.getAttachmentsAudioPrefix(), messageId, chatId, file);
        return uploadStrict(file, objectName, AttachmentMime.AUDIO, "audio attachment");
    }

    public String putAttachmentFile(UUID messageId, UUID chatId, MultipartFile file) {
        String objectName = buildAttachmentObjectName(props.getAttachmentsFilePrefix(), messageId, chatId, file);
        return uploadFileCategory(file, objectName);
    }


    private String uploadStrict(MultipartFile file, String objectName, AttachmentMime expected, String label) {
        try (InputStream in = file.getInputStream()) {
            if (file.isEmpty()) {
                throw new IllegalArgumentException(label + ": empty file");
            }

            String ct = AttachmentMime.normalize(file.getContentType());
            AttachmentMime actual = AttachmentMime.classify(ct);

            if (actual != expected) {
                throw new IllegalArgumentException(label + ": invalid content-type " + ct);
            }

            minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(props.getBucket())
                            .object(objectName)
                            .stream(in, file.getSize(), -1)
                            .contentType(ct)
                            .build()
            );
            return objectName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload " + label + " to MinIO", e);
        }
    }


    private String uploadFileCategory(MultipartFile file, String objectName) {
        try (InputStream in = file.getInputStream()) {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("file attachment: empty file");
            }

            String ct = AttachmentMime.normalize(file.getContentType());
            AttachmentMime actual = AttachmentMime.classify(ct);

            if (actual == AttachmentMime.IMAGE || actual == AttachmentMime.VIDEO || actual == AttachmentMime.AUDIO) {
                throw new IllegalArgumentException("file attachment: image/video/audio are not allowed here: " + ct);
            }
            if (actual == AttachmentMime.UNKNOWN) {
                throw new IllegalArgumentException("file attachment: unknown content-type");
            }

            minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(props.getBucket())
                            .object(objectName)
                            .stream(in, file.getSize(), -1)
                            .contentType(ct)
                            .build()
            );
            return objectName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file attachment to MinIO", e);
        }
    }

    public UploadedObject putAttachmentAnyTyped(UUID messageId, UUID chatId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("attachment: empty file");
        }

        String ct = AttachmentMime.normalize(file.getContentType());
        AttachmentMime kind = AttachmentMime.classify(ct);

        return switch (kind) {
            case IMAGE -> new UploadedObject(
                    putAttachmentImage(messageId, chatId, file),
                    AttachmentType.IMAGE
            );
            case VIDEO -> new UploadedObject(
                    putAttachmentVideo(messageId, chatId, file),
                    AttachmentType.VIDEO
            );
            case AUDIO -> new UploadedObject(
                    putAttachmentAudio(messageId, chatId, file),
                    AttachmentType.AUDIO
            );
            case FILE -> {
                String objectKey = putAttachmentFile(messageId, chatId, file);
                AttachmentType type = AttachmentMime.classifyFileAttachmentType(
                        file.getContentType(),
                        file.getOriginalFilename()
                );
                yield new UploadedObject(objectKey, type);
            }
            case UNKNOWN -> throw new IllegalArgumentException("attachment: unknown content-type");
        };
    }

    private String buildAttachmentObjectName(String prefix, UUID messageId, UUID chatId, MultipartFile file) {
        String original = file != null ? file.getOriginalFilename() : null;
        String name = sanitizeFileName(original != null ? original : "file");
        String unique = UUID.randomUUID().toString();
        return prefix + "/" + chatId + "/" + messageId + "/" + unique + "/" + name;
    }

    private String sanitizeFileName(String s) {
        String t = s.replace("\\", "_").replace("/", "_");
        t = t.replace("..", "_");
        t = t.trim();
        if (t.isEmpty()) return "file";
        return t.length() > 200 ? t.substring(t.length() - 200) : t;
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

    public @NotNull String getBucketNameSafe() {
        return props.getBucket();
    }
}
