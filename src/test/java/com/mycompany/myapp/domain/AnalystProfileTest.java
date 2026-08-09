package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.AnalystProfileTestSamples.*;
import static com.mycompany.myapp.domain.CompetitorTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AnalystProfileTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AnalystProfile.class);
        AnalystProfile analystProfile1 = getAnalystProfileSample1();
        AnalystProfile analystProfile2 = new AnalystProfile();
        assertThat(analystProfile1).isNotEqualTo(analystProfile2);

        analystProfile2.setId(analystProfile1.getId());
        assertThat(analystProfile1).isEqualTo(analystProfile2);

        analystProfile2 = getAnalystProfileSample2();
        assertThat(analystProfile1).isNotEqualTo(analystProfile2);
    }

    @Test
    void competitorsTest() {
        AnalystProfile analystProfile = getAnalystProfileRandomSampleGenerator();
        Competitor competitorBack = getCompetitorRandomSampleGenerator();

        analystProfile.addCompetitors(competitorBack);
        assertThat(analystProfile.getCompetitorses()).containsOnly(competitorBack);

        analystProfile.removeCompetitors(competitorBack);
        assertThat(analystProfile.getCompetitorses()).doesNotContain(competitorBack);

        analystProfile.competitorses(new HashSet<>(Set.of(competitorBack)));
        assertThat(analystProfile.getCompetitorses()).containsOnly(competitorBack);

        analystProfile.setCompetitorses(new HashSet<>());
        assertThat(analystProfile.getCompetitorses()).doesNotContain(competitorBack);
    }
}
