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
import tech.sh2rman.coreservice.domain.chat.model.NotificationSetting;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "chat_user_settings", schema = "uplink", indexes = {
        @Index(name = "idx_chat_user_settings_user_id",
                columnList = "user_id"),
        @Index(name = "idx_chat_user_settings_pinned",
                columnList = "is_pinned"),
        @Index(name = "idx_chat_user_settings_archived",
                columnList = "is_archived")}, uniqueConstraints = {@UniqueConstraint(name = "chat_user_settings_chat_id_user_id_key",
        columnNames = {
                "chat_id",
                "user_id"})})
public class ChatUserSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfileEntity user;

    @ColumnDefault("'ALL'")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "notification_setting", columnDefinition = "notification_setting not null")
    private NotificationSetting notificationSetting;

    @Column(name = "mute_until")
    private OffsetDateTime muteUntil;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "is_archived", nullable = false)
    private Boolean isArchived;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "hide_media", nullable = false)
    private Boolean hideMedia;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "hide_stickers", nullable = false)
    private Boolean hideStickers;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "hide_links_preview", nullable = false)
    private Boolean hideLinksPreview;

    @Size(max = 64)
    @Column(name = "custom_nickname", length = 64)
    private String customNickname;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;


}