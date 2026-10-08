package com.mycompany.myapp.service.mapper;

import static com.mycompany.myapp.domain.AnalystProfileAsserts.*;
import static com.mycompany.myapp.domain.AnalystProfileTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnalystProfileMapperTest {

    private AnalystProfileMapper analystProfileMapper;

    @BeforeEach
    void setUp() {
        analystProfileMapper = new AnalystProfileMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getAnalystProfileSample1();
        var actual = analystProfileMapper.toEntity(analystProfileMapper.toDto(expected));
        assertAnalystProfileAllPropertiesEquals(expected, actual);
    }
}
