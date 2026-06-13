package com.nexaflow.billing.service;

import com.nexaflow.billing.domain.*; // for static metamodels
import com.nexaflow.billing.domain.Plan;
import com.nexaflow.billing.repository.PlanRepository;
import com.nexaflow.billing.service.criteria.PlanCriteria;
import com.nexaflow.billing.service.dto.PlanDTO;
import com.nexaflow.billing.service.mapper.PlanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Plan} entities in the database.
 * The main input is a {@link PlanCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link PlanDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class PlanQueryService extends QueryService<Plan> {

    private static final Logger LOG = LoggerFactory.getLogger(PlanQueryService.class);

    private final PlanRepository planRepository;

    private final PlanMapper planMapper;

    public PlanQueryService(PlanRepository planRepository, PlanMapper planMapper) {
        this.planRepository = planRepository;
        this.planMapper = planMapper;
    }

    /**
     * Return a {@link Page} of {@link PlanDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<PlanDTO> findByCriteria(PlanCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Plan> specification = createSpecification(criteria);
        return planRepository.findAll(specification, page).map(planMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(PlanCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Plan> specification = createSpecification(criteria);
        return planRepository.count(specification);
    }

    /**
     * Function to convert {@link PlanCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Plan> createSpecification(PlanCriteria criteria) {
        Specification<Plan> specification = Specification.unrestricted();
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), Plan_.id),
                    buildSpecification(criteria.getCode(), Plan_.code),
                    buildStringSpecification(criteria.getName(), Plan_.name),
                    buildRangeSpecification(criteria.getPriceMonthly(), Plan_.priceMonthly),
                    buildRangeSpecification(criteria.getMaxProjects(), Plan_.maxProjects),
                    buildRangeSpecification(criteria.getMaxUsers(), Plan_.maxUsers),
                    buildRangeSpecification(criteria.getMaxTasks(), Plan_.maxTasks),
                    buildSpecification(criteria.getActive(), Plan_.active),
                    buildRangeSpecification(criteria.getCreatedAt(), Plan_.createdAt),
                    buildRangeSpecification(criteria.getUpdatedAt(), Plan_.updatedAt)
                )
            );
        }
        return specification;
    }
}
