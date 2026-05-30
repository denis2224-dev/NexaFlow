package com.nexaflow.userservice.service.mapper;

import static com.nexaflow.userservice.domain.MembershipAsserts.*;
import static com.nexaflow.userservice.domain.MembershipTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MembershipMapperTest {

    private MembershipMapper membershipMapper;

    @BeforeEach
    void setUp() {
        membershipMapper = new MembershipMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getMembershipSample1();
        var actual = membershipMapper.toEntity(membershipMapper.toDto(expected));
        assertMembershipAllPropertiesEquals(expected, actual);
    }
}
