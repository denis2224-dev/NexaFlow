package com.nexaflow.userservice.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexaflow.userservice.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MembershipDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(MembershipDTO.class);
        MembershipDTO membershipDTO1 = new MembershipDTO();
        membershipDTO1.setId(1L);
        MembershipDTO membershipDTO2 = new MembershipDTO();
        assertThat(membershipDTO1).isNotEqualTo(membershipDTO2);
        membershipDTO2.setId(membershipDTO1.getId());
        assertThat(membershipDTO1).isEqualTo(membershipDTO2);
        membershipDTO2.setId(2L);
        assertThat(membershipDTO1).isNotEqualTo(membershipDTO2);
        membershipDTO1.setId(null);
        assertThat(membershipDTO1).isNotEqualTo(membershipDTO2);
    }
}
