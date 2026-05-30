package com.nexaflow.userservice.domain;

import static com.nexaflow.userservice.domain.InvitationTestSamples.*;
import static com.nexaflow.userservice.domain.OrganizationTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.nexaflow.userservice.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class InvitationTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Invitation.class);
        Invitation invitation1 = getInvitationSample1();
        Invitation invitation2 = new Invitation();
        assertThat(invitation1).isNotEqualTo(invitation2);

        invitation2.setId(invitation1.getId());
        assertThat(invitation1).isEqualTo(invitation2);

        invitation2 = getInvitationSample2();
        assertThat(invitation1).isNotEqualTo(invitation2);
    }

    @Test
    void organizationTest() {
        Invitation invitation = getInvitationRandomSampleGenerator();
        Organization organizationBack = getOrganizationRandomSampleGenerator();

        invitation.setOrganization(organizationBack);
        assertThat(invitation.getOrganization()).isEqualTo(organizationBack);

        invitation.organization(null);
        assertThat(invitation.getOrganization()).isNull();
    }
}
