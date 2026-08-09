package com.mycompany.myapp.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class NewsItemDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(NewsItemDTO.class);
        NewsItemDTO newsItemDTO1 = new NewsItemDTO();
        newsItemDTO1.setId(1L);
        NewsItemDTO newsItemDTO2 = new NewsItemDTO();
        assertThat(newsItemDTO1).isNotEqualTo(newsItemDTO2);
        newsItemDTO2.setId(newsItemDTO1.getId());
        assertThat(newsItemDTO1).isEqualTo(newsItemDTO2);
        newsItemDTO2.setId(2L);
        assertThat(newsItemDTO1).isNotEqualTo(newsItemDTO2);
        newsItemDTO1.setId(null);
        assertThat(newsItemDTO1).isNotEqualTo(newsItemDTO2);
    }
}
