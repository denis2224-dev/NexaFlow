package com.nexaflow.project.service.mapper;

import static com.nexaflow.project.domain.ActivityLogAsserts.*;
import static com.nexaflow.project.domain.ActivityLogTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ActivityLogMapperTest {

    private ActivityLogMapper activityLogMapper;

    @BeforeEach
    void setUp() {
        activityLogMapper = new ActivityLogMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getActivityLogSample1();
        var actual = activityLogMapper.toEntity(activityLogMapper.toDto(expected));
        assertActivityLogAllPropertiesEquals(expected, actual);
    }
}
