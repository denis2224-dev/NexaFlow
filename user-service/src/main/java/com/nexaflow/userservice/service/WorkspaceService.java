package com.nexaflow.userservice.service;

import com.nexaflow.userservice.domain.Membership;
import com.nexaflow.userservice.domain.Organization;
import com.nexaflow.userservice.domain.enumeration.MembershipRole;
import com.nexaflow.userservice.repository.MembershipRepository;
import com.nexaflow.userservice.repository.OrganizationRepository;
import com.nexaflow.userservice.security.SecurityUtils;
import com.nexaflow.userservice.service.dto.CreateWorkspaceRequest;
import com.nexaflow.userservice.service.dto.WorkspaceDTO;
import com.nexaflow.userservice.web.rest.errors.BadRequestAlertException;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WorkspaceService {

    private static final String ENTITY_NAME = "workspace";

    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;

    public WorkspaceService(OrganizationRepository organizationRepository, MembershipRepository membershipRepository) {
        this.organizationRepository = organizationRepository;
        this.membershipRepository = membershipRepository;
    }

    public WorkspaceDTO createWorkspace(CreateWorkspaceRequest request) {
        String currentUserLogin = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() -> new BadRequestAlertException("Current user is not authenticated", ENTITY_NAME, "usernotauthenticated"));

        String normalizedSlug = request.getSlug().trim().toLowerCase();

        if (organizationRepository.existsBySlug(normalizedSlug)) {
            throw new BadRequestAlertException("A workspace with this slug already exists", ENTITY_NAME, "slugexists");
        }

        Organization organization = new Organization();
        organization.setName(request.getName().trim());
        organization.setSlug(normalizedSlug);
        organization.setDescription(request.getDescription());
        organization.setCreatedAt(Instant.now());
        organization.setActive(true);

        Organization savedOrganization = organizationRepository.save(organization);

        Membership ownerMembership = new Membership();
        ownerMembership.setOrganization(savedOrganization);
        ownerMembership.setUserId(0L);
        ownerMembership.setUserLogin(currentUserLogin);
        ownerMembership.setUserEmail(currentUserLogin + "@local");
        ownerMembership.setRole(MembershipRole.OWNER);
        ownerMembership.setJoinedAt(Instant.now());
        ownerMembership.setActive(true);

        membershipRepository.save(ownerMembership);

        return new WorkspaceDTO(
            savedOrganization.getId(),
            savedOrganization.getName(),
            savedOrganization.getSlug(),
            savedOrganization.getDescription(),
            MembershipRole.OWNER
        );
    }
}
