package com.nexaflow.userservice.service.dto;

import com.nexaflow.userservice.domain.enumeration.MembershipRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class InviteUserRequest {

    @Email
    @NotNull
    private String email;

    @NotNull
    private MembershipRole role;

    public String getEmail() {
        return email;
    }

    public MembershipRole getRole() {
        return role;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(MembershipRole role) {
        this.role = role;
    }
}
