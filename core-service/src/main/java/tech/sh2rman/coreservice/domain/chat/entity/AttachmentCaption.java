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
@Table(name = "attachment_caption", schema = "uplink", uniqueConstraints = {@UniqueConstraint(name = "attachment_caption_attachment_id_language_key",
        columnNames = {
                "attachment_id",
                "language"})})
public class AttachmentCaption {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "attachment_id", nullable = false)
    private Attachment attachment;

    @Column(name = "caption", length = Integer.MAX_VALUE)
    private String caption;

    @Size(max = 10)
    @NotNull
    @ColumnDefault("'en'")
    @Column(name = "language", nullable = false, length = 10)
    private String language;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;


}