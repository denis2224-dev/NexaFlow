package com.nexaflow.project.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class TaskTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Task getTaskSample1() {
        return new Task().id(1L).organizationId(1L).title("title1").assignedUserLogin("assignedUserLogin1").createdBy("createdBy1");
    }

    public static Task getTaskSample2() {
        return new Task().id(2L).organizationId(2L).title("title2").assignedUserLogin("assignedUserLogin2").createdBy("createdBy2");
    }

    public static Task getTaskRandomSampleGenerator() {
        return new Task()
            .id(longCount.incrementAndGet())
            .organizationId(longCount.incrementAndGet())
            .title(UUID.randomUUID().toString())
            .assignedUserLogin(UUID.randomUUID().toString())
            .createdBy(UUID.randomUUID().toString());
    }
}
