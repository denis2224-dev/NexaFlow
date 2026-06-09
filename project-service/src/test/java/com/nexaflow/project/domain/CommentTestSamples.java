package com.nexaflow.project.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class CommentTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Comment getCommentSample1() {
        return new Comment().id(1L).organizationId(1L).authorLogin("authorLogin1");
    }

    public static Comment getCommentSample2() {
        return new Comment().id(2L).organizationId(2L).authorLogin("authorLogin2");
    }

    public static Comment getCommentRandomSampleGenerator() {
        return new Comment()
            .id(longCount.incrementAndGet())
            .organizationId(longCount.incrementAndGet())
            .authorLogin(UUID.randomUUID().toString());
    }
}
