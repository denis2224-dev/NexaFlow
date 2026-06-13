package com.nexaflow.billing.service;

import com.nexaflow.billing.domain.*; // for static metamodels
import com.nexaflow.billing.domain.Subscription;
import com.nexaflow.billing.repository.SubscriptionRepository;
import com.nexaflow.billing.service.criteria.SubscriptionCriteria;
import com.nexaflow.billing.service.dto.SubscriptionDTO;
import com.nexaflow.billing.service.mapper.SubscriptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Subscription} entities in the database.
 * The main input is a {@link SubscriptionCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link SubscriptionDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class SubscriptionQueryService extends QueryService<Subscription> {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionQueryService.class);

    private final SubscriptionRepository subscriptionRepository;

    private final SubscriptionMapper subscriptionMapper;

    public SubscriptionQueryService(SubscriptionRepository subscriptionRepository, SubscriptionMapper subscriptionMapper) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionMapper = subscriptionMapper;
    }

    /**
     * Return a {@link Page} of {@link SubscriptionDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<SubscriptionDTO> findByCriteria(SubscriptionCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Subscription> specification = createSpecification(criteria);
        return subscriptionRepository.findAll(specification, page).map(subscriptionMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(SubscriptionCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Subscription> specification = createSpecification(criteria);
        return subscriptionRepository.count(specification);
    }

    /**
     * Function to convert {@link SubscriptionCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Subscription> createSpecification(SubscriptionCriteria criteria) {
        Specification<Subscription> specification = Specification.unrestricted();
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), Subscription_.id),
                    buildRangeSpecification(criteria.getOrganizationId(), Subscription_.organizationId),
                    buildSpecification(criteria.getPlanCode(), Subscription_.planCode),
                    buildSpecification(criteria.getStatus(), Subscription_.status),
                    buildRangeSpecification(criteria.getStartedAt(), Subscription_.startedAt),
                    buildRangeSpecification(criteria.getExpiresAt(), Subscription_.expiresAt),
                    buildStringSpecification(criteria.getCreatedBy(), Subscription_.createdBy),
                    buildRangeSpecification(criteria.getCreatedAt(), Subscription_.createdAt),
                    buildRangeSpecification(criteria.getUpdatedAt(), Subscription_.updatedAt)
                )
            );
        }
        return specification;
    }
}
