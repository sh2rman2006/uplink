package tech.sh2rman.coreservice.domain.chat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;
import tech.sh2rman.coreservice.domain.chat.model.ChatRole;
import tech.sh2rman.coreservice.domain.chat.model.ParticipantStatus;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "chat_participant", schema = "uplink", indexes = {
        @Index(name = "idx_chat_participant_chat_id",
                columnList = "chat_id"),
        @Index(name = "idx_chat_participant_user_id",
                columnList = "user_id"),
        @Index(name = "idx_chat_participant_role",
                columnList = "role"),
        @Index(name = "idx_chat_participant_status",
                columnList = "status"),
        @Index(name = "idx_chat_participant_banned_until",
                columnList = "banned_until")}, uniqueConstraints = {@UniqueConstraint(name = "chat_participant_chat_id_user_id_key",
        columnNames = {
                "chat_id",
                "user_id"})})
public class ChatParticipant {
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

    @ColumnDefault("'MEMBER'")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role", columnDefinition = "chat_role not null")
    private ChatRole role;

    @ColumnDefault("'ACTIVE'")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", columnDefinition = "participant_status not null")
    private ParticipantStatus status;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "can_send_messages", nullable = false)
    private Boolean canSendMessages;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "can_send_media", nullable = false)
    private Boolean canSendMedia;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "can_add_users", nullable = false)
    private Boolean canAddUsers;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "can_pin_messages", nullable = false)
    private Boolean canPinMessages;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "can_change_info", nullable = false)
    private Boolean canChangeInfo;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "can_delete_messages", nullable = false)
    private Boolean canDeleteMessages;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "can_ban_users", nullable = false)
    private Boolean canBanUsers;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "joined_at", nullable = false)
    private OffsetDateTime joinedAt;

    @Column(name = "left_at")
    private OffsetDateTime leftAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by")
    private UserProfileEntity invitedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banned_by")
    private UserProfileEntity bannedBy;

    @Column(name = "banned_until")
    private OffsetDateTime bannedUntil;

    @Column(name = "ban_reason", length = Integer.MAX_VALUE)
    private String banReason;

    @Column(name = "last_read_message_id")
    private UUID lastReadMessageId;

    @Column(name = "last_read_at")
    private OffsetDateTime lastReadAt;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;


}