package com.nexaflow.project.repository;

import com.nexaflow.project.domain.ActivityLog;
import com.nexaflow.project.domain.enumeration.ActivityEntityType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ActivityLog entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    Page<ActivityLog> findByOrganizationId(Long organizationId, Pageable pageable);

    Optional<ActivityLog> findOneByIdAndOrganizationId(Long id, Long organizationId);

    Page<ActivityLog> findByOrganizationIdAndEntityTypeAndEntityId(
        Long organizationId,
        ActivityEntityType entityType,
        Long entityId,
        Pageable pageable
    );
}
