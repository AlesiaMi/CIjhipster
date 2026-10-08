package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.AnalystProfileTestSamples.*;
import static com.mycompany.myapp.domain.CompetitorTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CompetitorTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Competitor.class);
        Competitor competitor1 = getCompetitorSample1();
        Competitor competitor2 = new Competitor();
        assertThat(competitor1).isNotEqualTo(competitor2);

        competitor2.setId(competitor1.getId());
        assertThat(competitor1).isEqualTo(competitor2);

        competitor2 = getCompetitorSample2();
        assertThat(competitor1).isNotEqualTo(competitor2);
    }

    @Test
    void analystProfilesTest() {
        Competitor competitor = getCompetitorRandomSampleGenerator();
        AnalystProfile analystProfileBack = getAnalystProfileRandomSampleGenerator();

        competitor.addAnalystProfiles(analystProfileBack);
        assertThat(competitor.getAnalystProfileses()).containsOnly(analystProfileBack);
        assertThat(analystProfileBack.getCompetitorses()).containsOnly(competitor);

        competitor.removeAnalystProfiles(analystProfileBack);
        assertThat(competitor.getAnalystProfileses()).doesNotContain(analystProfileBack);
        assertThat(analystProfileBack.getCompetitorses()).doesNotContain(competitor);

        competitor.analystProfileses(new HashSet<>(Set.of(analystProfileBack)));
        assertThat(competitor.getAnalystProfileses()).containsOnly(analystProfileBack);
        assertThat(analystProfileBack.getCompetitorses()).containsOnly(competitor);

        competitor.setAnalystProfileses(new HashSet<>());
        assertThat(competitor.getAnalystProfileses()).doesNotContain(analystProfileBack);
        assertThat(analystProfileBack.getCompetitorses()).doesNotContain(competitor);
    }
}
