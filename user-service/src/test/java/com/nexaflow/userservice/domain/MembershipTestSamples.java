package com.nexaflow.userservice.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class MembershipTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Membership getMembershipSample1() {
        return new Membership().id(1L).userId(1L).userLogin("userLogin1").userEmail("userEmail1");
    }

    public static Membership getMembershipSample2() {
        return new Membership().id(2L).userId(2L).userLogin("userLogin2").userEmail("userEmail2");
    }

    public static Membership getMembershipRandomSampleGenerator() {
        return new Membership()
            .id(longCount.incrementAndGet())
            .userId(longCount.incrementAndGet())
            .userLogin(UUID.randomUUID().toString())
            .userEmail(UUID.randomUUID().toString());
    }
}
