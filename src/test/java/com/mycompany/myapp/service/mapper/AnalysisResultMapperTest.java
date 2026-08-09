package com.mycompany.myapp.service.mapper;

import static com.mycompany.myapp.domain.AnalysisResultAsserts.*;
import static com.mycompany.myapp.domain.AnalysisResultTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnalysisResultMapperTest {

    private AnalysisResultMapper analysisResultMapper;

    @BeforeEach
    void setUp() {
        analysisResultMapper = new AnalysisResultMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getAnalysisResultSample1();
        var actual = analysisResultMapper.toEntity(analysisResultMapper.toDto(expected));
        assertAnalysisResultAllPropertiesEquals(expected, actual);
    }
}
