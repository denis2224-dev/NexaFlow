package com.nexaflow.project.repository;

import com.nexaflow.project.domain.Task;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@SuppressWarnings("unused")
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByOrganizationId(Long organizationId, Pageable pageable);

    Optional<Task> findOneByIdAndOrganizationId(Long id, Long organizationId);
}
