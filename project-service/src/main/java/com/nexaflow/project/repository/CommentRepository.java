package com.nexaflow.project.repository;

import com.nexaflow.project.domain.Comment;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Comment entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByOrganizationId(Long organizationId, Pageable pageable);

    Optional<Comment> findOneByIdAndOrganizationId(Long id, Long organizationId);

    Page<Comment> findByTaskId(Long taskId, Pageable pageable);

    @Modifying
    @Query("delete from Comment comment where comment.task.id = :taskId")
    void deleteByTaskId(@Param("taskId") Long taskId);

    @Modifying
    @Query("delete from Comment comment where comment.task.project.id = :projectId")
    void deleteByTaskProjectId(@Param("projectId") Long projectId);
}
