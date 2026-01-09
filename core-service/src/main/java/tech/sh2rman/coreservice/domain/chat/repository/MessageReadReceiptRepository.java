package tech.sh2rman.coreservice.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.sh2rman.coreservice.domain.chat.entity.MessageReadReceipt;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageReadReceiptRepository extends JpaRepository<MessageReadReceipt, UUID> {
    Optional<MessageReadReceipt> findByMessageIdAndUserId(UUID messageId, UUID userId);

    @Modifying
    @Query(value = """
        INSERT INTO uplink.message_read_receipt (id, message_id, user_id, read_at)
        SELECT gen_random_uuid(), m.id, :userId, :readAt
        FROM uplink.message m
        WHERE m.chat_id = :chatId
          AND m.deleted_at IS NULL
          AND m.sender_id <> :userId
          AND m.created_at <= (
              SELECT m2.created_at
              FROM uplink.message m2
              WHERE m2.id = :upToMessageId
                AND m2.chat_id = :chatId
                AND m2.deleted_at IS NULL
          )
        ON CONFLICT (message_id, user_id) DO NOTHING
        """, nativeQuery = true)
    int insertReceiptsUpTo(
            @Param("chatId") UUID chatId,
            @Param("userId") UUID userId,
            @Param("upToMessageId") UUID upToMessageId,
            @Param("readAt") OffsetDateTime readAt
    );
}