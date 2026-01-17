package tech.sh2rman.coreservice.domain.chat.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.sh2rman.coreservice.domain.chat.entity.ChatParticipant;
import tech.sh2rman.coreservice.domain.chat.model.ChatRole;
import tech.sh2rman.coreservice.domain.chat.model.ParticipantStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, UUID> {
    Optional<ChatParticipant> findByChatIdAndUserId(UUID chatId, UUID userId);

    long countByChatIdAndRoleAndStatus(UUID chatId, ChatRole chatRole, ParticipantStatus participantStatus);

    List<ChatParticipant> findByUserIdAndChatIdIn(UUID userId, Collection<UUID> chatIds);

    @EntityGraph(attributePaths = {"user"})
    Page<ChatParticipant> findByChatId(UUID chatId, Pageable pageable);

    List<ChatParticipant> findByChatIdIn(Collection<UUID> ids);

}