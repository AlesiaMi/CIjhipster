package com.mycompany.myapp.service.mapper;

import static com.mycompany.myapp.domain.KeywordAsserts.*;
import static com.mycompany.myapp.domain.KeywordTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KeywordMapperTest {

    private KeywordMapper keywordMapper;

    @BeforeEach
    void setUp() {
        keywordMapper = new KeywordMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getKeywordSample1();
        var actual = keywordMapper.toEntity(keywordMapper.toDto(expected));
        assertKeywordAllPropertiesEquals(expected, actual);
    }
}
