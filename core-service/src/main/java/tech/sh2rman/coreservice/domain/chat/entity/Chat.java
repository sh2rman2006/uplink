package tech.sh2rman.coreservice.domain.chat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tech.sh2rman.coreservice.domain.chat.model.ChatType;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "chat", schema = "uplink", indexes = {
        @Index(name = "idx_chat_type",
                columnList = "type"),
        @Index(name = "idx_chat_is_private",
                columnList = "is_private"),
        @Index(name = "idx_chat_is_public",
                columnList = "is_public"),
        @Index(name = "idx_chat_created_by",
                columnList = "created_by"),
        @Index(name = "idx_chat_updated_at",
                columnList = "updated_at"),
        @Index(name = "idx_chat_last_message_at",
                columnList = "last_message_at")}, uniqueConstraints = {@UniqueConstraint(name = "chat_invite_link_key",
        columnNames = {"invite_link"})})
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", columnDefinition = "chat_type not null")
    private ChatType type;

    @Size(max = 255)
    @Column(name = "title")
    private String title;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Size(max = 512)
    @Column(name = "avatar_object_key", length = 512)
    private String avatarObjectKey;

    @Size(max = 1024)
    @Column(name = "avatar_url", length = 1024)
    private String avatarUrl;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @Size(max = 255)
    @Column(name = "invite_link")
    private String inviteLink;

    @Column(name = "invite_link_expires_at")
    private OffsetDateTime inviteLinkExpiresAt;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "allow_send_messages", nullable = false)
    private Boolean allowSendMessages;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "allow_send_media", nullable = false)
    private Boolean allowSendMedia;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "allow_add_users", nullable = false)
    private Boolean allowAddUsers;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "allow_pin_messages", nullable = false)
    private Boolean allowPinMessages;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "allow_change_info", nullable = false)
    private Boolean allowChangeInfo;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "is_encrypted", nullable = false)
    private Boolean isEncrypted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UserProfileEntity createdBy;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "last_message_at")
    private OffsetDateTime lastMessageAt;

    @Column(name = "last_message_id")
    private UUID lastMessageId;

    @OneToMany(mappedBy = "chat")
    private Set<ChatJoinRequest> chatJoinRequests = new LinkedHashSet<>();

    @OneToMany(mappedBy = "chat")
    private Set<ChatParticipant> chatParticipants = new LinkedHashSet<>();

    @OneToMany(mappedBy = "chat")
    private Set<ChatUserSetting> chatUserSettings = new LinkedHashSet<>();

    @OneToMany(mappedBy = "chat")
    private Set<Message> messages = new LinkedHashSet<>();


}