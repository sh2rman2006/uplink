package tech.sh2rman.coreservice.domain.chat.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.sh2rman.coreservice.domain.chat.entity.ChatJoinRequest;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatJoinRequestRepository extends JpaRepository<ChatJoinRequest, UUID> {
    Optional<ChatJoinRequest> findByChat_IdAndUser_Id(UUID chatId, UUID userId);

    Page<ChatJoinRequest> findByUser_IdAndStatusOrderByCreatedAtDesc(UUID userId, String status, Pageable pageable);

    Page<ChatJoinRequest> findByChat_IdAndStatusOrderByCreatedAtDesc(UUID chatId, String status, Pageable pageable);

}