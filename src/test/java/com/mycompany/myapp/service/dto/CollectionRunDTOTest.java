package com.mycompany.myapp.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CollectionRunDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CollectionRunDTO.class);
        CollectionRunDTO collectionRunDTO1 = new CollectionRunDTO();
        collectionRunDTO1.setId(1L);
        CollectionRunDTO collectionRunDTO2 = new CollectionRunDTO();
        assertThat(collectionRunDTO1).isNotEqualTo(collectionRunDTO2);
        collectionRunDTO2.setId(collectionRunDTO1.getId());
        assertThat(collectionRunDTO1).isEqualTo(collectionRunDTO2);
        collectionRunDTO2.setId(2L);
        assertThat(collectionRunDTO1).isNotEqualTo(collectionRunDTO2);
        collectionRunDTO1.setId(null);
        assertThat(collectionRunDTO1).isNotEqualTo(collectionRunDTO2);
    }
}
