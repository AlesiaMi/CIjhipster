package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.CompetitorTestSamples.*;
import static com.mycompany.myapp.domain.DataSourceTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DataSourceTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DataSource.class);
        DataSource dataSource1 = getDataSourceSample1();
        DataSource dataSource2 = new DataSource();
        assertThat(dataSource1).isNotEqualTo(dataSource2);

        dataSource2.setId(dataSource1.getId());
        assertThat(dataSource1).isEqualTo(dataSource2);

        dataSource2 = getDataSourceSample2();
        assertThat(dataSource1).isNotEqualTo(dataSource2);
    }

    @Test
    void competitorTest() {
        DataSource dataSource = getDataSourceRandomSampleGenerator();
        Competitor competitorBack = getCompetitorRandomSampleGenerator();

        dataSource.setCompetitor(competitorBack);
        assertThat(dataSource.getCompetitor()).isEqualTo(competitorBack);

        dataSource.competitor(null);
        assertThat(dataSource.getCompetitor()).isNull();
    }
}
