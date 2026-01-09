package tech.sh2rman.coreservice.domain.chat.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.sh2rman.coreservice.domain.chat.entity.Message;

import java.time.OffsetDateTime;
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
          and (:lastReadAt is null or m.createdAt > :lastReadAt)
    """)
    long countUnread(
            @Param("chatId") UUID chatId,
            @Param("userId") UUID userId,
            @Param("lastReadAt") OffsetDateTime lastReadAt
    );
}