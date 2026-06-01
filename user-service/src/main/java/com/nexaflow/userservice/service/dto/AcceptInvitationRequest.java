package com.nexaflow.userservice.service.dto;

import jakarta.validation.constraints.NotBlank;

public class AcceptInvitationRequest {

    @NotBlank
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
