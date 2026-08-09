package com.mycompany.myapp.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class KeywordDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(KeywordDTO.class);
        KeywordDTO keywordDTO1 = new KeywordDTO();
        keywordDTO1.setId(1L);
        KeywordDTO keywordDTO2 = new KeywordDTO();
        assertThat(keywordDTO1).isNotEqualTo(keywordDTO2);
        keywordDTO2.setId(keywordDTO1.getId());
        assertThat(keywordDTO1).isEqualTo(keywordDTO2);
        keywordDTO2.setId(2L);
        assertThat(keywordDTO1).isNotEqualTo(keywordDTO2);
        keywordDTO1.setId(null);
        assertThat(keywordDTO1).isNotEqualTo(keywordDTO2);
    }
}
