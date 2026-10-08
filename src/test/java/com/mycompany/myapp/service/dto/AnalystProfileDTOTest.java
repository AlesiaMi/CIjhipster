package com.mycompany.myapp.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AnalystProfileDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AnalystProfileDTO.class);
        AnalystProfileDTO analystProfileDTO1 = new AnalystProfileDTO();
        analystProfileDTO1.setId(1L);
        AnalystProfileDTO analystProfileDTO2 = new AnalystProfileDTO();
        assertThat(analystProfileDTO1).isNotEqualTo(analystProfileDTO2);
        analystProfileDTO2.setId(analystProfileDTO1.getId());
        assertThat(analystProfileDTO1).isEqualTo(analystProfileDTO2);
        analystProfileDTO2.setId(2L);
        assertThat(analystProfileDTO1).isNotEqualTo(analystProfileDTO2);
        analystProfileDTO1.setId(null);
        assertThat(analystProfileDTO1).isNotEqualTo(analystProfileDTO2);
    }
}
