package tech.sh2rman.coreservice.domain.chat.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.sh2rman.coreservice.domain.chat.entity.Message;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findTop50ByChatIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID chatId);

    List<Message> findTop50ByChatIdAndDeletedAtIsNullAndCreatedAtLessThanOrderByCreatedAtDesc(
            UUID chatId,
            OffsetDateTime before
    );

    @EntityGraph(attributePaths = "chat")
    Optional<Message> findByIdAndChatId(UUID id, UUID chatId);

    Optional<Message> findFirstByChatIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID chatId);

    @Query("""
        select count(m)
        from Message m
        where m.chat.id = :chatId
          and m.deletedAt is null
          and m.sender.id <> :userId
    """)
    long countUnreadAll(
            @Param("chatId") UUID chatId,
            @Param("userId") UUID userId
    );

    @Query("""
        select count(m)
        from Message m
        where m.chat.id = :chatId
          and m.deletedAt is null
          and m.sender.id <> :userId
          and m.createdAt > :lastReadAt
    """)
    long countUnreadAfter(
            @Param("chatId") UUID chatId,
            @Param("userId") UUID userId,
            @Param("lastReadAt") OffsetDateTime lastReadAt
    );

    default long countUnread(UUID chatId, UUID userId, OffsetDateTime lastReadAt) {
        if (lastReadAt == null) {
            return countUnreadAll(chatId, userId);
        }
        return countUnreadAfter(chatId, userId, lastReadAt);
    }

    @Query(value = """
        select m.chat_id as chatId,
               count(*) as unread
        from uplink.message m
        join uplink.chat_participant cp
          on cp.chat_id = m.chat_id
         and cp.user_id = :userId
        where m.chat_id in (:chatIds)
          and m.deleted_at is null
          and m.sender_id <> :userId
          and m.created_at > coalesce(cp.last_read_at, to_timestamp(0))
        group by m.chat_id
    """, nativeQuery = true)
    List<ChatUnreadCountProjection> countUnreadByChats(
            @Param("userId") UUID userId,
            @Param("chatIds") Collection<UUID> chatIds
    );

}