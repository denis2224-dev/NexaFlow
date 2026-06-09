package com.nexaflow.project.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ProjectTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Project getProjectSample1() {
        return new Project().id(1L).organizationId(1L).name("name1").createdBy("createdBy1");
    }

    public static Project getProjectSample2() {
        return new Project().id(2L).organizationId(2L).name("name2").createdBy("createdBy2");
    }

    public static Project getProjectRandomSampleGenerator() {
        return new Project()
            .id(longCount.incrementAndGet())
            .organizationId(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .createdBy(UUID.randomUUID().toString());
    }
}
