package com.mycompany.myapp.service.mapper;

import static com.mycompany.myapp.domain.CollectionRunAsserts.*;
import static com.mycompany.myapp.domain.CollectionRunTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CollectionRunMapperTest {

    private CollectionRunMapper collectionRunMapper;

    @BeforeEach
    void setUp() {
        collectionRunMapper = new CollectionRunMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getCollectionRunSample1();
        var actual = collectionRunMapper.toEntity(collectionRunMapper.toDto(expected));
        assertCollectionRunAllPropertiesEquals(expected, actual);
    }
}
