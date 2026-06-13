package com.nexaflow.billing.service.mapper;

import static com.nexaflow.billing.domain.SubscriptionAsserts.*;
import static com.nexaflow.billing.domain.SubscriptionTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SubscriptionMapperTest {

    private SubscriptionMapper subscriptionMapper;

    @BeforeEach
    void setUp() {
        subscriptionMapper = new SubscriptionMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getSubscriptionSample1();
        var actual = subscriptionMapper.toEntity(subscriptionMapper.toDto(expected));
        assertSubscriptionAllPropertiesEquals(expected, actual);
    }
}
