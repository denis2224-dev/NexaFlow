package com.nexaflow.userservice.service.mapper;

import com.nexaflow.userservice.domain.Invitation;
import com.nexaflow.userservice.domain.Organization;
import com.nexaflow.userservice.service.dto.InvitationDTO;
import com.nexaflow.userservice.service.dto.OrganizationDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Invitation} and its DTO {@link InvitationDTO}.
 */
@Mapper(componentModel = "spring")
public interface InvitationMapper extends EntityMapper<InvitationDTO, Invitation> {
    @Mapping(target = "organization", source = "organization", qualifiedByName = "organizationName")
    InvitationDTO toDto(Invitation s);

    @Named("organizationName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    OrganizationDTO toDtoOrganizationName(Organization organization);
}
