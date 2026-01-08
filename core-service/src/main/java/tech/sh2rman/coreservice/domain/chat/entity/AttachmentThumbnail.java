package tech.sh2rman.coreservice.domain.chat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "attachment_thumbnail", schema = "uplink", uniqueConstraints = {@UniqueConstraint(name = "attachment_thumbnail_attachment_id_size_key",
        columnNames = {
                "attachment_id",
                "size"})})
public class AttachmentThumbnail {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "attachment_id", nullable = false)
    private Attachment attachment;

    @Size(max = 20)
    @NotNull
    @Column(name = "size", nullable = false, length = 20)
    private String size;

    @NotNull
    @Column(name = "width", nullable = false)
    private Integer width;

    @NotNull
    @Column(name = "height", nullable = false)
    private Integer height;

    @Size(max = 1024)
    @NotNull
    @Column(name = "object_key", nullable = false, length = 1024)
    private String objectKey;

    @Size(max = 2048)
    @Column(name = "url", length = 2048)
    private String url;

    @NotNull
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;


}