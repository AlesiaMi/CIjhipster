package com.mycompany.myapp.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CiAlertDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CiAlertDTO.class);
        CiAlertDTO ciAlertDTO1 = new CiAlertDTO();
        ciAlertDTO1.setId(1L);
        CiAlertDTO ciAlertDTO2 = new CiAlertDTO();
        assertThat(ciAlertDTO1).isNotEqualTo(ciAlertDTO2);
        ciAlertDTO2.setId(ciAlertDTO1.getId());
        assertThat(ciAlertDTO1).isEqualTo(ciAlertDTO2);
        ciAlertDTO2.setId(2L);
        assertThat(ciAlertDTO1).isNotEqualTo(ciAlertDTO2);
        ciAlertDTO1.setId(null);
        assertThat(ciAlertDTO1).isNotEqualTo(ciAlertDTO2);
    }
}
