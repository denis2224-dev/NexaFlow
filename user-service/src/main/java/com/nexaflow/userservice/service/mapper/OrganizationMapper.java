package com.nexaflow.userservice.service.mapper;

import com.nexaflow.userservice.domain.Organization;
import com.nexaflow.userservice.service.dto.OrganizationDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Organization} and its DTO {@link OrganizationDTO}.
 */
@Mapper(componentModel = "spring")
public interface OrganizationMapper extends EntityMapper<OrganizationDTO, Organization> {}
