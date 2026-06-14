package com.nexaflow.billing.security;

import com.nexaflow.billing.client.WorkspaceClient;
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
}
