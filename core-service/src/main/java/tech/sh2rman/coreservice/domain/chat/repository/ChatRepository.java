package tech.sh2rman.coreservice.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.sh2rman.coreservice.domain.chat.entity.Chat;
import tech.sh2rman.coreservice.domain.chat.model.ChatType;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {
    @Query("""
      select c
      from Chat c
      join c.chatParticipants p
      where c.type = :type
        and p.user.id in (:userA, :userB)
      group by c
      having count(distinct p.user.id) = 2
    """)
    Optional<Chat> findChatByTypeAndBothUsers(
            @Param("type") ChatType type,
            @Param("userA") UUID userA,
            @Param("userB") UUID userB
    );
}