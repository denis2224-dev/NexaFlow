package com.nexaflow.userservice.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class InvitationTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Invitation getInvitationSample1() {
        return new Invitation().id(1L).email("email1").token("token1").invitedByUserId(1L).invitedByLogin("invitedByLogin1");
    }

    public static Invitation getInvitationSample2() {
        return new Invitation().id(2L).email("email2").token("token2").invitedByUserId(2L).invitedByLogin("invitedByLogin2");
    }

    public static Invitation getInvitationRandomSampleGenerator() {
        return new Invitation()
            .id(longCount.incrementAndGet())
            .email(UUID.randomUUID().toString())
            .token(UUID.randomUUID().toString())
            .invitedByUserId(longCount.incrementAndGet())
            .invitedByLogin(UUID.randomUUID().toString());
    }
}
