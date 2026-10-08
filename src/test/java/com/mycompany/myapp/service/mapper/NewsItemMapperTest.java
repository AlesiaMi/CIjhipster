package com.mycompany.myapp.service.mapper;

import static com.mycompany.myapp.domain.NewsItemAsserts.*;
import static com.mycompany.myapp.domain.NewsItemTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NewsItemMapperTest {

    private NewsItemMapper newsItemMapper;

    @BeforeEach
    void setUp() {
        newsItemMapper = new NewsItemMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getNewsItemSample1();
        var actual = newsItemMapper.toEntity(newsItemMapper.toDto(expected));
        assertNewsItemAllPropertiesEquals(expected, actual);
    }
}
