package com.nexaflow.userservice.service;

import com.nexaflow.userservice.domain.*; // for static metamodels
import com.nexaflow.userservice.domain.Membership;
import com.nexaflow.userservice.repository.MembershipRepository;
import com.nexaflow.userservice.service.criteria.MembershipCriteria;
import com.nexaflow.userservice.service.dto.MembershipDTO;
import com.nexaflow.userservice.service.mapper.MembershipMapper;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Membership} entities in the database.
 * The main input is a {@link MembershipCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link MembershipDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class MembershipQueryService extends QueryService<Membership> {

    private static final Logger LOG = LoggerFactory.getLogger(MembershipQueryService.class);

    private final MembershipRepository membershipRepository;

    private final MembershipMapper membershipMapper;

    public MembershipQueryService(MembershipRepository membershipRepository, MembershipMapper membershipMapper) {
        this.membershipRepository = membershipRepository;
        this.membershipMapper = membershipMapper;
    }

    /**
     * Return a {@link Page} of {@link MembershipDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<MembershipDTO> findByCriteria(MembershipCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Membership> specification = createSpecification(criteria);
        return membershipRepository.findAll(specification, page).map(membershipMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(MembershipCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Membership> specification = createSpecification(criteria);
        return membershipRepository.count(specification);
    }

    /**
     * Function to convert {@link MembershipCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Membership> createSpecification(MembershipCriteria criteria) {
        Specification<Membership> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Membership_.organization, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), Membership_.id),
                    buildRangeSpecification(criteria.getUserId(), Membership_.userId),
                    buildStringSpecification(criteria.getUserLogin(), Membership_.userLogin),
                    buildStringSpecification(criteria.getUserEmail(), Membership_.userEmail),
                    buildSpecification(criteria.getRole(), Membership_.role),
                    buildRangeSpecification(criteria.getJoinedAt(), Membership_.joinedAt),
                    buildSpecification(criteria.getActive(), Membership_.active),
                    buildSpecification(criteria.getOrganizationId(), root ->
                        root.join(Membership_.organization, JoinType.LEFT).get(Organization_.id)
                    )
                )
            );
        }
        return specification;
    }
}
