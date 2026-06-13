package com.nexaflow.billing.domain;

import static com.nexaflow.billing.domain.SubscriptionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.nexaflow.billing.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SubscriptionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Subscription.class);
        Subscription subscription1 = getSubscriptionSample1();
        Subscription subscription2 = new Subscription();
        assertThat(subscription1).isNotEqualTo(subscription2);

        subscription2.setId(subscription1.getId());
        assertThat(subscription1).isEqualTo(subscription2);

        subscription2 = getSubscriptionSample2();
        assertThat(subscription1).isNotEqualTo(subscription2);
    }
}
