package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.AnalysisResultTestSamples.*;
import static com.mycompany.myapp.domain.NewsItemTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AnalysisResultTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AnalysisResult.class);
        AnalysisResult analysisResult1 = getAnalysisResultSample1();
        AnalysisResult analysisResult2 = new AnalysisResult();
        assertThat(analysisResult1).isNotEqualTo(analysisResult2);

        analysisResult2.setId(analysisResult1.getId());
        assertThat(analysisResult1).isEqualTo(analysisResult2);

        analysisResult2 = getAnalysisResultSample2();
        assertThat(analysisResult1).isNotEqualTo(analysisResult2);
    }

    @Test
    void newsItemTest() {
        AnalysisResult analysisResult = getAnalysisResultRandomSampleGenerator();
        NewsItem newsItemBack = getNewsItemRandomSampleGenerator();

        analysisResult.setNewsItem(newsItemBack);
        assertThat(analysisResult.getNewsItem()).isEqualTo(newsItemBack);

        analysisResult.newsItem(null);
        assertThat(analysisResult.getNewsItem()).isNull();
    }
}
