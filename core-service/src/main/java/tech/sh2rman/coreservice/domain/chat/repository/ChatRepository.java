package tech.sh2rman.coreservice.domain.chat.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.sh2rman.coreservice.domain.chat.entity.Chat;
import tech.sh2rman.coreservice.domain.chat.model.ChatType;
import tech.sh2rman.coreservice.domain.chat.model.ParticipantStatus;

import java.util.Collection;
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
        group by c.id
        having count(distinct p.user.id) = 2
    """)
    Optional<Chat> findChatByTypeAndBothUsers(
            @Param("type") ChatType type,
            @Param("userA") UUID userA,
            @Param("userB") UUID userB
    );

    @Query("""
        select c
        from Chat c
        join c.chatParticipants p
        where p.user.id = :userId
          and p.status in :statuses
        order by coalesce(c.lastMessageAt, c.updatedAt) desc
    """)
    Page<Chat> findMyChats(
            @Param("userId") UUID userId,
            @Param("statuses") Collection<ParticipantStatus> statuses,
            Pageable pageable
    );

    @Query("""
        select c
        from Chat c
        join c.chatParticipants p
        where p.user.id = :userId
          and p.status in :statuses
          and (
                (c.title is not null and lower(c.title) like lower(concat('%', :q, '%')))
             or (c.description is not null and lower(c.description) like lower(concat('%', :q, '%')))
          )
        order by coalesce(c.lastMessageAt, c.updatedAt) desc
    """)
    Page<Chat> searchMyChats(
            @Param("userId") UUID userId,
            @Param("statuses") Collection<ParticipantStatus> statuses,
            @Param("q") String q,
            Pageable pageable
    );
}