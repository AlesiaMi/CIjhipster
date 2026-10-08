package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.AnalysisResultTestSamples.*;
import static com.mycompany.myapp.domain.AnalystProfileTestSamples.*;
import static com.mycompany.myapp.domain.CiAlertTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CiAlertTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CiAlert.class);
        CiAlert ciAlert1 = getCiAlertSample1();
        CiAlert ciAlert2 = new CiAlert();
        assertThat(ciAlert1).isNotEqualTo(ciAlert2);

        ciAlert2.setId(ciAlert1.getId());
        assertThat(ciAlert1).isEqualTo(ciAlert2);

        ciAlert2 = getCiAlertSample2();
        assertThat(ciAlert1).isNotEqualTo(ciAlert2);
    }

    @Test
    void analysisResultTest() {
        CiAlert ciAlert = getCiAlertRandomSampleGenerator();
        AnalysisResult analysisResultBack = getAnalysisResultRandomSampleGenerator();

        ciAlert.setAnalysisResult(analysisResultBack);
        assertThat(ciAlert.getAnalysisResult()).isEqualTo(analysisResultBack);

        ciAlert.analysisResult(null);
        assertThat(ciAlert.getAnalysisResult()).isNull();
    }

    @Test
    void analystProfileTest() {
        CiAlert ciAlert = getCiAlertRandomSampleGenerator();
        AnalystProfile analystProfileBack = getAnalystProfileRandomSampleGenerator();

        ciAlert.setAnalystProfile(analystProfileBack);
        assertThat(ciAlert.getAnalystProfile()).isEqualTo(analystProfileBack);

        ciAlert.analystProfile(null);
        assertThat(ciAlert.getAnalystProfile()).isNull();
    }
}
