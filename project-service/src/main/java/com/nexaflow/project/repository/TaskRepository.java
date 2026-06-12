package com.nexaflow.project.repository;

import com.nexaflow.project.domain.Task;
import com.nexaflow.project.domain.enumeration.TaskStatus;
import java.time.LocalDate;
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

    Page<Task> findByProjectId(Long projectId, Pageable pageable);

    Page<Task> findByOrganizationIdAndAssignedUserLogin(Long organizationId, String assignedUserLogin, Pageable pageable);

    Page<Task> findByOrganizationIdAndDueDate(Long organizationId, LocalDate dueDate, Pageable pageable);

    Page<Task> findByOrganizationIdAndDueDateBeforeAndStatusNot(
        Long organizationId,
        LocalDate dueDate,
        TaskStatus status,
        Pageable pageable
    );

    long countByOrganizationId(Long organizationId);

    long countByOrganizationIdAndStatus(Long organizationId, TaskStatus status);

    long countByOrganizationIdAndDueDateBeforeAndStatusNot(Long organizationId, LocalDate dueDate, TaskStatus status);
}
