package com.nexaflow.userservice.service;

import com.nexaflow.userservice.domain.*; // for static metamodels
import com.nexaflow.userservice.domain.Invitation;
import com.nexaflow.userservice.repository.InvitationRepository;
import com.nexaflow.userservice.service.criteria.InvitationCriteria;
import com.nexaflow.userservice.service.dto.InvitationDTO;
import com.nexaflow.userservice.service.mapper.InvitationMapper;
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
 * Service for executing complex queries for {@link Invitation} entities in the database.
 * The main input is a {@link InvitationCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link InvitationDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class InvitationQueryService extends QueryService<Invitation> {

    private static final Logger LOG = LoggerFactory.getLogger(InvitationQueryService.class);

    private final InvitationRepository invitationRepository;

    private final InvitationMapper invitationMapper;

    public InvitationQueryService(InvitationRepository invitationRepository, InvitationMapper invitationMapper) {
        this.invitationRepository = invitationRepository;
        this.invitationMapper = invitationMapper;
    }

    /**
     * Return a {@link Page} of {@link InvitationDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<InvitationDTO> findByCriteria(InvitationCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Invitation> specification = createSpecification(criteria);
        return invitationRepository.findAll(specification, page).map(invitationMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(InvitationCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Invitation> specification = createSpecification(criteria);
        return invitationRepository.count(specification);
    }

    /**
     * Function to convert {@link InvitationCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Invitation> createSpecification(InvitationCriteria criteria) {
        Specification<Invitation> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Invitation_.organization, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), Invitation_.id),
                    buildStringSpecification(criteria.getEmail(), Invitation_.email),
                    buildStringSpecification(criteria.getToken(), Invitation_.token),
                    buildSpecification(criteria.getRole(), Invitation_.role),
                    buildSpecification(criteria.getStatus(), Invitation_.status),
                    buildRangeSpecification(criteria.getInvitedAt(), Invitation_.invitedAt),
                    buildRangeSpecification(criteria.getExpiresAt(), Invitation_.expiresAt),
                    buildRangeSpecification(criteria.getAcceptedAt(), Invitation_.acceptedAt),
                    buildRangeSpecification(criteria.getInvitedByUserId(), Invitation_.invitedByUserId),
                    buildStringSpecification(criteria.getInvitedByLogin(), Invitation_.invitedByLogin),
                    buildSpecification(criteria.getOrganizationId(), root ->
                        root.join(Invitation_.organization, JoinType.LEFT).get(Organization_.id)
                    )
                )
            );
        }
        return specification;
    }
}
