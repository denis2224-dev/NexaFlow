package com.nexaflow.userservice.service.mapper;

import static com.nexaflow.userservice.domain.InvitationAsserts.*;
import static com.nexaflow.userservice.domain.InvitationTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InvitationMapperTest {

    private InvitationMapper invitationMapper;

    @BeforeEach
    void setUp() {
        invitationMapper = new InvitationMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getInvitationSample1();
        var actual = invitationMapper.toEntity(invitationMapper.toDto(expected));
        assertInvitationAllPropertiesEquals(expected, actual);
    }
}
