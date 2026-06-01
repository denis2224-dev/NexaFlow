package com.nexaflow.userservice.service.dto;

import com.nexaflow.userservice.domain.enumeration.MembershipRole;
import jakarta.validation.constraints.NotNull;

public class ChangeMemberRoleRequest {

    @NotNull
    private MembershipRole role;

    public MembershipRole getRole() {
        return role;
    }

    public void setRole(MembershipRole role) {
        this.role = role;
    }
}
