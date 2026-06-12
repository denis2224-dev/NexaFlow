package com.nexaflow.project.repository;

import com.nexaflow.project.domain.Comment;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
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
}
