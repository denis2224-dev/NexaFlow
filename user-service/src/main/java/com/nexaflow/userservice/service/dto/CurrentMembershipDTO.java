package com.nexaflow.userservice.service.dto;

import com.nexaflow.userservice.domain.enumeration.MembershipRole;

public record CurrentMembershipDTO(Long organizationId, MembershipRole role) {}
