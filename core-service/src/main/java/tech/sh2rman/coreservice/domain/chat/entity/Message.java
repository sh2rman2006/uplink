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
import tech.sh2rman.coreservice.domain.chat.model.MessageStatus;
import tech.sh2rman.coreservice.domain.chat.model.MessageType;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "message", schema = "uplink", indexes = {
        @Index(name = "idx_message_chat_created",
                columnList = "chat_id, created_at"),
        @Index(name = "idx_message_chat_id",
                columnList = "chat_id"),
        @Index(name = "idx_message_sender_id",
                columnList = "sender_id"),
        @Index(name = "idx_message_type",
                columnList = "type"),
        @Index(name = "idx_message_reply_to",
                columnList = "reply_to_message_id"),
        @Index(name = "idx_message_is_forwarded",
                columnList = "is_forwarded"),
        @Index(name = "idx_message_status",
                columnList = "status"),
        @Index(name = "idx_message_created_at",
                columnList = "created_at"),
        @Index(name = "idx_message_deleted_at",
                columnList = "deleted_at")})
public class Message {
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
    @JoinColumn(name = "sender_id", nullable = false)
    private UserProfileEntity sender;

    @ColumnDefault("'TEXT'")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", columnDefinition = "message_type not null")
    private MessageType type;

    @Column(name = "text", length = Integer.MAX_VALUE)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_message_id")
    private Message replyToMessage;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "is_forwarded", nullable = false)
    private Boolean isForwarded;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_sender_id")
    private UserProfileEntity originalSender;

    @Column(name = "original_chat_id")
    private UUID originalChatId;

    @Column(name = "original_message_id")
    private UUID originalMessageId;

    @Size(max = 100)
    @Column(name = "system_action", length = 100)
    private String systemAction;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "system_parameters")
    private Map<String, Object> systemParameters;

    @ColumnDefault("'SENT'")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", columnDefinition = "message_status not null")
    private MessageStatus status;

    @Column(name = "error_message", length = Integer.MAX_VALUE)
    private String errorMessage;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private UserProfileEntity deletedBy;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "edit_count", nullable = false)
    private Integer editCount;

    @Column(name = "last_edited_at")
    private OffsetDateTime lastEditedAt;

    @Column(name = "voice_duration")
    private Integer voiceDuration;

    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @Size(max = 255)
    @Column(name = "location_name")
    private String locationName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_user_id")
    private UserProfileEntity contactUser;

    @Size(max = 255)
    @Column(name = "contact_name")
    private String contactName;

    @OneToMany(mappedBy = "message")
    private Set<Attachment> attachments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "replyToMessage")
    private Set<Message> messages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "message")
    private Set<MessageReadReceipt> messageReadReceipts = new LinkedHashSet<>();

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;

        if (isForwarded == null) isForwarded = false;
        if (editCount == null) editCount = 0;

        if (type == null) type = MessageType.TEXT;
        if (status == null) status = MessageStatus.SENT;
    }


}