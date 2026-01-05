package tech.sh2rman.coreservice.domain.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "user_profile", schema = "uplink", indexes = {
        @Index(name = "uk_user_profile_username",
                columnList = "username",
                unique = true),
        @Index(name = "ix_user_profile_created_at",
                columnList = "created_at"),
        @Index(name = "ix_user_profile_last_seen_at",
                columnList = "last_seen_at")})
public class UserProfileEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 32)
    @Column(name = "username", length = 32)
    private String username;

    @Size(max = 254)
    @Column(name = "email", length = 254)
    private String email;

    @Size(max = 64)
    @Column(name = "display_name", length = 64)
    private String displayName;

    @Size(max = 280)
    @Column(name = "bio", length = 280)
    private String bio;

    @Size(max = 512)
    @Column(name = "avatar_object_key", length = 512)
    private String avatarObjectKey;

    @Size(max = 1024)
    @Column(name = "avatar_url", length = 1024)
    private String avatarUrl;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "avatar_version", nullable = false)
    private Long avatarVersion;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "is_profile_public", nullable = false)
    private Boolean isProfilePublic;

    @Size(max = 16)
    @NotNull
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "last_seen_at")
    private OffsetDateTime lastSeenAt;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

    @PrePersist
    void prePersist() {
        var now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;

        if (avatarVersion == null) avatarVersion = 1L;
        if (isProfilePublic == null) isProfilePublic = true;
        if (status == null) status = "ACTIVE";
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

}