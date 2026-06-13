package com.nexaflow.billing.service.mapper;

import com.nexaflow.billing.domain.Subscription;
import com.nexaflow.billing.service.dto.SubscriptionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Subscription} and its DTO {@link SubscriptionDTO}.
 */
@Mapper(componentModel = "spring")
public interface SubscriptionMapper extends EntityMapper<SubscriptionDTO, Subscription> {}
