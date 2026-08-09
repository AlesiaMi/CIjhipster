package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.CompetitorTestSamples.*;
import static com.mycompany.myapp.domain.KeywordTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class KeywordTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Keyword.class);
        Keyword keyword1 = getKeywordSample1();
        Keyword keyword2 = new Keyword();
        assertThat(keyword1).isNotEqualTo(keyword2);

        keyword2.setId(keyword1.getId());
        assertThat(keyword1).isEqualTo(keyword2);

        keyword2 = getKeywordSample2();
        assertThat(keyword1).isNotEqualTo(keyword2);
    }

    @Test
    void competitorTest() {
        Keyword keyword = getKeywordRandomSampleGenerator();
        Competitor competitorBack = getCompetitorRandomSampleGenerator();

        keyword.setCompetitor(competitorBack);
        assertThat(keyword.getCompetitor()).isEqualTo(competitorBack);

        keyword.competitor(null);
        assertThat(keyword.getCompetitor()).isNull();
    }
}
