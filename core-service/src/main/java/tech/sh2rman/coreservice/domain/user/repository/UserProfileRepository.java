package tech.sh2rman.coreservice.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;

import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, UUID> {
}
