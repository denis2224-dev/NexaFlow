package com.nexaflow.project.repository;

import com.nexaflow.project.domain.Project;
import com.nexaflow.project.domain.enumeration.ProjectStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@SuppressWarnings("unused")
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findByOrganizationId(Long organizationId, Pageable pageable);

    Optional<Project> findOneByIdAndOrganizationId(Long id, Long organizationId);

    long countByOrganizationId(Long organizationId);

    long countByOrganizationIdAndStatus(Long organizationId, ProjectStatus status);
}
