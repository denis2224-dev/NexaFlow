package com.nexaflow.userservice.service.dto;

import com.nexaflow.userservice.domain.enumeration.MembershipRole;

public class MemberDTO {

    private Long membershipId;
    private Long userId;
    private String userLogin;
    private String userEmail;
    private MembershipRole role;
    private Boolean active;

    public MemberDTO() {}

    public MemberDTO(Long membershipId, Long userId, String userLogin, String userEmail, MembershipRole role, Boolean active) {
        this.membershipId = membershipId;
        this.userId = userId;
        this.userLogin = userLogin;
        this.userEmail = userEmail;
        this.role = role;
        this.active = active;
    }

    public Long getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(Long membershipId) {
        this.membershipId = membershipId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public MembershipRole getRole() {
        return role;
    }

    public Boolean getActive() {
        return active;
    }
}
