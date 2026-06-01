package com.nexaflow.userservice.repository;

import com.nexaflow.userservice.domain.Membership;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Membership entity.
 */
@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long>, JpaSpecificationExecutor<Membership> {
    default Optional<Membership> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Membership> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Membership> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select membership from Membership membership left join fetch membership.organization",
        countQuery = "select count(membership) from Membership membership"
    )
    Page<Membership> findAllWithToOneRelationships(Pageable pageable);

    @Query("select membership from Membership membership left join fetch membership.organization")
    List<Membership> findAllWithToOneRelationships();

    @Query("select membership from Membership membership left join fetch membership.organization where membership.id =:id")
    Optional<Membership> findOneWithToOneRelationships(@Param("id") Long id);

    List<Membership> findByUserLoginAndActiveTrue(String userLogin);

    Optional<Membership> findOneByOrganizationIdAndUserLoginAndActiveTrue(Long organizationId, String userLogin);

    boolean existsByOrganizationIdAndUserEmailAndActiveTrue(Long organizationId, String userEmail);

    List<Membership> findByOrganizationIdAndActiveTrue(Long organizationId);

    Optional<Membership> findOneByIdAndOrganizationIdAndActiveTrue(Long membershipId, Long organizationId);
}
