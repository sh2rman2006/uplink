package tech.sh2rman.coreservice.domain.chat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;
import tech.sh2rman.coreservice.domain.chat.model.AttachmentType;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "attachment", schema = "uplink", indexes = {
        @Index(name = "idx_attachment_message_id",
                columnList = "message_id"),
        @Index(name = "idx_attachment_type",
                columnList = "type"),
        @Index(name = "idx_attachment_upload_status",
                columnList = "upload_status"),
        @Index(name = "idx_attachment_created_at",
                columnList = "created_at")}, uniqueConstraints = {@UniqueConstraint(name = "attachment_object_key_key",
        columnNames = {"object_key"})})
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", columnDefinition = "attachment_type not null")
    private AttachmentType type;

    @Size(max = 255)
    @NotNull
    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Size(max = 512)
    @Column(name = "file_name", length = 512)
    private String fileName;

    @NotNull
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Size(max = 1024)
    @NotNull
    @Column(name = "object_key", nullable = false, length = 1024)
    private String objectKey;

    @Size(max = 255)
    @NotNull
    @ColumnDefault("'uplink-media'")
    @Column(name = "bucket_name", nullable = false)
    private String bucketName;

    @Size(max = 2048)
    @Column(name = "url", length = 2048)
    private String url;

    @Size(max = 2048)
    @Column(name = "thumbnail_url", length = 2048)
    private String thumbnailUrl;

    @Size(max = 2048)
    @Column(name = "preview_url", length = 2048)
    private String previewUrl;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "has_thumbnail", nullable = false)
    private Boolean hasThumbnail;

    @Column(name = "thumbnail_width")
    private Integer thumbnailWidth;

    @Column(name = "thumbnail_height")
    private Integer thumbnailHeight;

    @Column(name = "page_count")
    private Integer pageCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "waveform")
    private Map<String, Object> waveform;

    @Size(max = 50)
    @NotNull
    @ColumnDefault("'PENDING'")
    @Column(name = "upload_status", nullable = false, length = 50)
    private String uploadStatus;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "upload_progress", nullable = false)
    private Integer uploadProgress;

    @Column(name = "uploaded_at")
    private OffsetDateTime uploadedAt;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "attachment")
    private Set<AttachmentCaption> attachmentCaptions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "attachment")
    private Set<AttachmentThumbnail> attachmentThumbnails = new LinkedHashSet<>();


}