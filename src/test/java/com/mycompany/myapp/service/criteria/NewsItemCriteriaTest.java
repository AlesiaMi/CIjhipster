package com.mycompany.myapp.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class NewsItemCriteriaTest {

    @Test
    void newNewsItemCriteriaHasAllFiltersNullTest() {
        var newsItemCriteria = new NewsItemCriteria();
        assertThat(newsItemCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void newsItemCriteriaFluentMethodsCreatesFiltersTest() {
        var newsItemCriteria = new NewsItemCriteria();

        setAllFilters(newsItemCriteria);

        assertThat(newsItemCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void newsItemCriteriaCopyCreatesNullFilterTest() {
        var newsItemCriteria = new NewsItemCriteria();
        var copy = newsItemCriteria.copy();

        assertThat(newsItemCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(newsItemCriteria)
        );
    }

    @Test
    void newsItemCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var newsItemCriteria = new NewsItemCriteria();
        setAllFilters(newsItemCriteria);

        var copy = newsItemCriteria.copy();

        assertThat(newsItemCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(newsItemCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var newsItemCriteria = new NewsItemCriteria();

        assertThat(newsItemCriteria).hasToString("NewsItemCriteria{}");
    }

    private static void setAllFilters(NewsItemCriteria newsItemCriteria) {
        newsItemCriteria.id();
        newsItemCriteria.externalId();
        newsItemCriteria.title();
        newsItemCriteria.url();
        newsItemCriteria.originalText();
        newsItemCriteria.publishedAt();
        newsItemCriteria.collectedAt();
        newsItemCriteria.isDuplicate();
        newsItemCriteria.dataSourceId();
        newsItemCriteria.competitorId();
        newsItemCriteria.collectionRunId();
        newsItemCriteria.analysisResultId();
        newsItemCriteria.distinct();
    }

    private static Condition<NewsItemCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getExternalId()) &&
                condition.apply(criteria.getTitle()) &&
                condition.apply(criteria.getUrl()) &&
                condition.apply(criteria.getOriginalText()) &&
                condition.apply(criteria.getPublishedAt()) &&
                condition.apply(criteria.getCollectedAt()) &&
                condition.apply(criteria.getIsDuplicate()) &&
                condition.apply(criteria.getDataSourceId()) &&
                condition.apply(criteria.getCompetitorId()) &&
                condition.apply(criteria.getCollectionRunId()) &&
                condition.apply(criteria.getAnalysisResultId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<NewsItemCriteria> copyFiltersAre(NewsItemCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getExternalId(), copy.getExternalId()) &&
                condition.apply(criteria.getTitle(), copy.getTitle()) &&
                condition.apply(criteria.getUrl(), copy.getUrl()) &&
                condition.apply(criteria.getOriginalText(), copy.getOriginalText()) &&
                condition.apply(criteria.getPublishedAt(), copy.getPublishedAt()) &&
                condition.apply(criteria.getCollectedAt(), copy.getCollectedAt()) &&
                condition.apply(criteria.getIsDuplicate(), copy.getIsDuplicate()) &&
                condition.apply(criteria.getDataSourceId(), copy.getDataSourceId()) &&
                condition.apply(criteria.getCompetitorId(), copy.getCompetitorId()) &&
                condition.apply(criteria.getCollectionRunId(), copy.getCollectionRunId()) &&
                condition.apply(criteria.getAnalysisResultId(), copy.getAnalysisResultId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
