package com.mycompany.myapp.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CompetitorDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CompetitorDTO.class);
        CompetitorDTO competitorDTO1 = new CompetitorDTO();
        competitorDTO1.setId(1L);
        CompetitorDTO competitorDTO2 = new CompetitorDTO();
        assertThat(competitorDTO1).isNotEqualTo(competitorDTO2);
        competitorDTO2.setId(competitorDTO1.getId());
        assertThat(competitorDTO1).isEqualTo(competitorDTO2);
        competitorDTO2.setId(2L);
        assertThat(competitorDTO1).isNotEqualTo(competitorDTO2);
        competitorDTO1.setId(null);
        assertThat(competitorDTO1).isNotEqualTo(competitorDTO2);
    }
}
