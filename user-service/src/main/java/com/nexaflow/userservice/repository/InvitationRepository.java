package com.nexaflow.userservice.repository;

import com.nexaflow.userservice.domain.Invitation;
import com.nexaflow.userservice.domain.enumeration.InvitationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Invitation entity.
 */
@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long>, JpaSpecificationExecutor<Invitation> {
    default Optional<Invitation> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Invitation> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Invitation> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select invitation from Invitation invitation left join fetch invitation.organization",
        countQuery = "select count(invitation) from Invitation invitation"
    )
    Page<Invitation> findAllWithToOneRelationships(Pageable pageable);

    @Query("select invitation from Invitation invitation left join fetch invitation.organization")
    List<Invitation> findAllWithToOneRelationships();

    @Query("select invitation from Invitation invitation left join fetch invitation.organization where invitation.id =:id")
    Optional<Invitation> findOneWithToOneRelationships(@Param("id") Long id);

    boolean existsByOrganizationIdAndEmailAndStatus(Long organizationId, String email, InvitationStatus status);

    List<Invitation> findByOrganizationIdAndStatus(Long organizationId, InvitationStatus status);

    Optional<Invitation> findOneByToken(String token);

    Optional<Invitation> findOneByIdAndOrganizationIdAndStatus(Long invitationId, Long organizationId, InvitationStatus status);
}
