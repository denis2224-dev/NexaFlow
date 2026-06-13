package com.nexaflow.billing.service.mapper;

import static com.nexaflow.billing.domain.PlanAsserts.*;
import static com.nexaflow.billing.domain.PlanTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlanMapperTest {

    private PlanMapper planMapper;

    @BeforeEach
    void setUp() {
        planMapper = new PlanMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getPlanSample1();
        var actual = planMapper.toEntity(planMapper.toDto(expected));
        assertPlanAllPropertiesEquals(expected, actual);
    }
}
