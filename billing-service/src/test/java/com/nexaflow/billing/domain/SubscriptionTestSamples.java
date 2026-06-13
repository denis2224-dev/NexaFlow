package com.nexaflow.billing.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class SubscriptionTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Subscription getSubscriptionSample1() {
        return new Subscription().id(1L).organizationId(1L).createdBy("createdBy1");
    }

    public static Subscription getSubscriptionSample2() {
        return new Subscription().id(2L).organizationId(2L).createdBy("createdBy2");
    }

    public static Subscription getSubscriptionRandomSampleGenerator() {
        return new Subscription()
            .id(longCount.incrementAndGet())
            .organizationId(longCount.incrementAndGet())
            .createdBy(UUID.randomUUID().toString());
    }
}
