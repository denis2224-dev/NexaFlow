package com.nexaflow.billing.repository;

import com.nexaflow.billing.domain.Subscription;
import com.nexaflow.billing.domain.enumeration.SubscriptionStatus;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Subscription entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long>, JpaSpecificationExecutor<Subscription> {
    List<Subscription> findByOrganizationIdAndStatusOrderByStartedAtDesc(Long organizationId, SubscriptionStatus status);
}
