package tech.sh2rman.coreservice.domain.chat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "message_read_receipt", schema = "uplink", indexes = {
        @Index(name = "idx_message_read_receipt_user_id",
                columnList = "user_id"),
        @Index(name = "idx_message_read_receipt_read_at",
                columnList = "read_at")}, uniqueConstraints = {@UniqueConstraint(name = "message_read_receipt_message_id_user_id_key",
        columnNames = {
                "message_id",
                "user_id"})})
public class MessageReadReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfileEntity user;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "read_at", nullable = false)
    private OffsetDateTime readAt;


}