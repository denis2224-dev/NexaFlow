package com.nexaflow.billing.service.mapper;

import com.nexaflow.billing.domain.Plan;
import com.nexaflow.billing.service.dto.PlanDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Plan} and its DTO {@link PlanDTO}.
 */
@Mapper(componentModel = "spring")
public interface PlanMapper extends EntityMapper<PlanDTO, Plan> {}
