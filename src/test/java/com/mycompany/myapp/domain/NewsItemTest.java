package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.AnalysisResultTestSamples.*;
import static com.mycompany.myapp.domain.CollectionRunTestSamples.*;
import static com.mycompany.myapp.domain.CompetitorTestSamples.*;
import static com.mycompany.myapp.domain.DataSourceTestSamples.*;
import static com.mycompany.myapp.domain.NewsItemTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class NewsItemTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(NewsItem.class);
        NewsItem newsItem1 = getNewsItemSample1();
        NewsItem newsItem2 = new NewsItem();
        assertThat(newsItem1).isNotEqualTo(newsItem2);

        newsItem2.setId(newsItem1.getId());
        assertThat(newsItem1).isEqualTo(newsItem2);

        newsItem2 = getNewsItemSample2();
        assertThat(newsItem1).isNotEqualTo(newsItem2);
    }

    @Test
    void dataSourceTest() {
        NewsItem newsItem = getNewsItemRandomSampleGenerator();
        DataSource dataSourceBack = getDataSourceRandomSampleGenerator();

        newsItem.setDataSource(dataSourceBack);
        assertThat(newsItem.getDataSource()).isEqualTo(dataSourceBack);

        newsItem.dataSource(null);
        assertThat(newsItem.getDataSource()).isNull();
    }

    @Test
    void competitorTest() {
        NewsItem newsItem = getNewsItemRandomSampleGenerator();
        Competitor competitorBack = getCompetitorRandomSampleGenerator();

        newsItem.setCompetitor(competitorBack);
        assertThat(newsItem.getCompetitor()).isEqualTo(competitorBack);

        newsItem.competitor(null);
        assertThat(newsItem.getCompetitor()).isNull();
    }

    @Test
    void collectionRunTest() {
        NewsItem newsItem = getNewsItemRandomSampleGenerator();
        CollectionRun collectionRunBack = getCollectionRunRandomSampleGenerator();

        newsItem.setCollectionRun(collectionRunBack);
        assertThat(newsItem.getCollectionRun()).isEqualTo(collectionRunBack);

        newsItem.collectionRun(null);
        assertThat(newsItem.getCollectionRun()).isNull();
    }

    @Test
    void analysisResultTest() {
        NewsItem newsItem = getNewsItemRandomSampleGenerator();
        AnalysisResult analysisResultBack = getAnalysisResultRandomSampleGenerator();

        newsItem.setAnalysisResult(analysisResultBack);
        assertThat(newsItem.getAnalysisResult()).isEqualTo(analysisResultBack);
        assertThat(analysisResultBack.getNewsItem()).isEqualTo(newsItem);

        newsItem.analysisResult(null);
        assertThat(newsItem.getAnalysisResult()).isNull();
        assertThat(analysisResultBack.getNewsItem()).isNull();
    }
}
