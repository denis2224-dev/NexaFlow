package com.nexaflow.project.security;

import com.nexaflow.project.client.WorkspaceClient;
import com.nexaflow.project.client.dto.MembershipRole;
import feign.FeignException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class OrganizationAccessService {

    private final WorkspaceClient workspaceClient;

    public OrganizationAccessService(WorkspaceClient workspaceClient) {
        this.workspaceClient = workspaceClient;
    }

    public void assertMember(Long organizationId) {
        if (organizationId == null) {
            throw new AccessDeniedException("Organization is required");
        }

        try {
            workspaceClient.getCurrentMembership(organizationId);
        } catch (FeignException.Forbidden | FeignException.NotFound e) {
            throw new AccessDeniedException("You are not a member of this organization");
        }
    }

    public void assertAdminOrOwner(Long organizationId) {
        try {
            MembershipRole role = workspaceClient.getCurrentMembership(organizationId).role();

            if (role != MembershipRole.ADMIN && role != MembershipRole.OWNER) {
                throw new AccessDeniedException("Admin or owner role required");
            }
        } catch (FeignException.Forbidden | FeignException.NotFound e) {
            throw new AccessDeniedException("You are not a member of this organization");
        }
    }
}
