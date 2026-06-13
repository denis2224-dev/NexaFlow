package com.nexaflow.billing.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PlanTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Plan getPlanSample1() {
        return new Plan().id(1L).name("name1").maxProjects(1).maxUsers(1).maxTasks(1);
    }

    public static Plan getPlanSample2() {
        return new Plan().id(2L).name("name2").maxProjects(2).maxUsers(2).maxTasks(2);
    }

    public static Plan getPlanRandomSampleGenerator() {
        return new Plan()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .maxProjects(intCount.incrementAndGet())
            .maxUsers(intCount.incrementAndGet())
            .maxTasks(intCount.incrementAndGet());
    }
}
