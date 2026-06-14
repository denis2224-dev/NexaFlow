package com.nexaflow.billing.repository;

import com.nexaflow.billing.domain.Plan;
import com.nexaflow.billing.domain.enumeration.PlanCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Plan entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PlanRepository extends JpaRepository<Plan, Long>, JpaSpecificationExecutor<Plan> {
    List<Plan> findAllByActiveTrueOrderByPriceMonthlyAsc();

    Optional<Plan> findByCodeAndActiveTrue(PlanCode code);
}
