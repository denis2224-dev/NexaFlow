package com.nexaflow.userservice.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexaflow.userservice.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class InvitationDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(InvitationDTO.class);
        InvitationDTO invitationDTO1 = new InvitationDTO();
        invitationDTO1.setId(1L);
        InvitationDTO invitationDTO2 = new InvitationDTO();
        assertThat(invitationDTO1).isNotEqualTo(invitationDTO2);
        invitationDTO2.setId(invitationDTO1.getId());
        assertThat(invitationDTO1).isEqualTo(invitationDTO2);
        invitationDTO2.setId(2L);
        assertThat(invitationDTO1).isNotEqualTo(invitationDTO2);
        invitationDTO1.setId(null);
        assertThat(invitationDTO1).isNotEqualTo(invitationDTO2);
    }
}
