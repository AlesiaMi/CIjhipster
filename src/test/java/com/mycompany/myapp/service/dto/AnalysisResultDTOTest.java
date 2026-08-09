package com.mycompany.myapp.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AnalysisResultDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AnalysisResultDTO.class);
        AnalysisResultDTO analysisResultDTO1 = new AnalysisResultDTO();
        analysisResultDTO1.setId(1L);
        AnalysisResultDTO analysisResultDTO2 = new AnalysisResultDTO();
        assertThat(analysisResultDTO1).isNotEqualTo(analysisResultDTO2);
        analysisResultDTO2.setId(analysisResultDTO1.getId());
        assertThat(analysisResultDTO1).isEqualTo(analysisResultDTO2);
        analysisResultDTO2.setId(2L);
        assertThat(analysisResultDTO1).isNotEqualTo(analysisResultDTO2);
        analysisResultDTO1.setId(null);
        assertThat(analysisResultDTO1).isNotEqualTo(analysisResultDTO2);
    }
}
