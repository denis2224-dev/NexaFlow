package com.nexaflow.userservice.domain;

import static com.nexaflow.userservice.domain.MembershipTestSamples.*;
import static com.nexaflow.userservice.domain.OrganizationTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.nexaflow.userservice.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MembershipTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Membership.class);
        Membership membership1 = getMembershipSample1();
        Membership membership2 = new Membership();
        assertThat(membership1).isNotEqualTo(membership2);

        membership2.setId(membership1.getId());
        assertThat(membership1).isEqualTo(membership2);

        membership2 = getMembershipSample2();
        assertThat(membership1).isNotEqualTo(membership2);
    }

    @Test
    void organizationTest() {
        Membership membership = getMembershipRandomSampleGenerator();
        Organization organizationBack = getOrganizationRandomSampleGenerator();

        membership.setOrganization(organizationBack);
        assertThat(membership.getOrganization()).isEqualTo(organizationBack);

        membership.organization(null);
        assertThat(membership.getOrganization()).isNull();
    }
}
