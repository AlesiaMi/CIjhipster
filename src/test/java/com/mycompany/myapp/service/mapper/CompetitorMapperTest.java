package com.mycompany.myapp.service.mapper;

import static com.mycompany.myapp.domain.CompetitorAsserts.*;
import static com.mycompany.myapp.domain.CompetitorTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CompetitorMapperTest {

    private CompetitorMapper competitorMapper;

    @BeforeEach
    void setUp() {
        competitorMapper = new CompetitorMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getCompetitorSample1();
        var actual = competitorMapper.toEntity(competitorMapper.toDto(expected));
        assertCompetitorAllPropertiesEquals(expected, actual);
    }
}
