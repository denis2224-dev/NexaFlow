package com.nexaflow.project.service.mapper;

import com.nexaflow.project.domain.ActivityLog;
import com.nexaflow.project.service.dto.ActivityLogDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ActivityLog} and its DTO {@link ActivityLogDTO}.
 */
@Mapper(componentModel = "spring")
public interface ActivityLogMapper extends EntityMapper<ActivityLogDTO, ActivityLog> {}
