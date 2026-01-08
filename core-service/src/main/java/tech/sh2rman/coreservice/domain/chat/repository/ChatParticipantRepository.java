package tech.sh2rman.coreservice.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.sh2rman.coreservice.domain.chat.entity.ChatParticipant;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, UUID> {
    Optional<ChatParticipant> findByChatIdAndUserId(UUID chatId, UUID userId);
}