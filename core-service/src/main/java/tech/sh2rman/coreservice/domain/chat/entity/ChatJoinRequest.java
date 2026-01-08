package tech.sh2rman.coreservice.domain.chat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "chat_join_request", schema = "uplink", indexes = {
        @Index(name = "idx_chat_join_request_chat_id",
                columnList = "chat_id"),
        @Index(name = "idx_chat_join_request_status",
                columnList = "status")}, uniqueConstraints = {@UniqueConstraint(name = "chat_join_request_chat_id_user_id_key",
        columnNames = {
                "chat_id",
                "user_id"})})
public class ChatJoinRequest {
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

    @Column(name = "message", length = Integer.MAX_VALUE)
    private String message;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'PENDING'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private UserProfileEntity reviewedBy;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;


}