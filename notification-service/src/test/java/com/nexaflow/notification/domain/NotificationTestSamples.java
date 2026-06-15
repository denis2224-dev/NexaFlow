package com.nexaflow.notification.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class NotificationTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Notification getNotificationSample1() {
        return new Notification().id(1L).organizationId(1L).recipientLogin("recipientLogin1").title("title1").sourceId(1L);
    }

    public static Notification getNotificationSample2() {
        return new Notification().id(2L).organizationId(2L).recipientLogin("recipientLogin2").title("title2").sourceId(2L);
    }

    public static Notification getNotificationRandomSampleGenerator() {
        return new Notification()
            .id(longCount.incrementAndGet())
            .organizationId(longCount.incrementAndGet())
            .recipientLogin(UUID.randomUUID().toString())
            .title(UUID.randomUUID().toString())
            .sourceId(longCount.incrementAndGet());
    }
}
