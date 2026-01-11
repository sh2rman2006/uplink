package tech.sh2rman.coreservice.domain.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;

import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, UUID> {
    @Query("""
                select u
                from UserProfileEntity u
                where u.status = 'ACTIVE'
                  and (
                        (u.username is not null and lower(u.username) like lower(concat('%', :q, '%')))
                     or (u.displayName is not null and lower(u.displayName) like lower(concat('%', :q, '%')))
                  )
            """)
    Page<UserProfileEntity> searchActive(@Param("q") String q, Pageable pageable);
}
