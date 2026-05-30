package com.nexaflow.userservice.service.mapper;

import com.nexaflow.userservice.domain.Membership;
import com.nexaflow.userservice.domain.Organization;
import com.nexaflow.userservice.service.dto.MembershipDTO;
import com.nexaflow.userservice.service.dto.OrganizationDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Membership} and its DTO {@link MembershipDTO}.
 */
@Mapper(componentModel = "spring")
public interface MembershipMapper extends EntityMapper<MembershipDTO, Membership> {
    @Mapping(target = "organization", source = "organization", qualifiedByName = "organizationName")
    MembershipDTO toDto(Membership s);

    @Named("organizationName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    OrganizationDTO toDtoOrganizationName(Organization organization);
}
