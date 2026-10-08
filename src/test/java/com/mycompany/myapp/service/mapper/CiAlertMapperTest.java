package com.mycompany.myapp.service.mapper;

import static com.mycompany.myapp.domain.CiAlertAsserts.*;
import static com.mycompany.myapp.domain.CiAlertTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CiAlertMapperTest {

    private CiAlertMapper ciAlertMapper;

    @BeforeEach
    void setUp() {
        ciAlertMapper = new CiAlertMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getCiAlertSample1();
        var actual = ciAlertMapper.toEntity(ciAlertMapper.toDto(expected));
        assertCiAlertAllPropertiesEquals(expected, actual);
    }
}
