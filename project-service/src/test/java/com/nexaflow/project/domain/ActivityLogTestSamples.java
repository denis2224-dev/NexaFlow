package com.nexaflow.project.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ActivityLogTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static ActivityLog getActivityLogSample1() {
        return new ActivityLog().id(1L).organizationId(1L).entityId(1L).performedBy("performedBy1");
    }

    public static ActivityLog getActivityLogSample2() {
        return new ActivityLog().id(2L).organizationId(2L).entityId(2L).performedBy("performedBy2");
    }

    public static ActivityLog getActivityLogRandomSampleGenerator() {
        return new ActivityLog()
            .id(longCount.incrementAndGet())
            .organizationId(longCount.incrementAndGet())
            .entityId(longCount.incrementAndGet())
            .performedBy(UUID.randomUUID().toString());
    }
}
