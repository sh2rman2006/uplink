package tech.sh2rman.coreservice.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.sh2rman.coreservice.domain.chat.entity.Message;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findTop50ByChatIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID chatId);

    List<Message> findTop50ByChatIdAndDeletedAtIsNullAndCreatedAtLessThanOrderByCreatedAtDesc(
            UUID chatId,
            OffsetDateTime before
    );
}