package com.mycompany.myapp.service.mapper;

import static com.mycompany.myapp.domain.DataSourceAsserts.*;
import static com.mycompany.myapp.domain.DataSourceTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataSourceMapperTest {

    private DataSourceMapper dataSourceMapper;

    @BeforeEach
    void setUp() {
        dataSourceMapper = new DataSourceMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getDataSourceSample1();
        var actual = dataSourceMapper.toEntity(dataSourceMapper.toDto(expected));
        assertDataSourceAllPropertiesEquals(expected, actual);
    }
}
