package tech.sh2rman.coreservice.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.sh2rman.coreservice.domain.chat.entity.AttachmentThumbnail;

import java.util.UUID;

@Repository
public interface AttachmentThumbnailRepository extends JpaRepository<AttachmentThumbnail, UUID> {
}