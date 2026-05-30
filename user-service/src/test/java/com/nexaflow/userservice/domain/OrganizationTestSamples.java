package com.nexaflow.userservice.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class OrganizationTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Organization getOrganizationSample1() {
        return new Organization().id(1L).name("name1").slug("slug1").description("description1");
    }

    public static Organization getOrganizationSample2() {
        return new Organization().id(2L).name("name2").slug("slug2").description("description2");
    }

    public static Organization getOrganizationRandomSampleGenerator() {
        return new Organization()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .slug(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString());
    }
}
