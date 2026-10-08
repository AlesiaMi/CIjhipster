package com.mycompany.myapp.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class AnalysisResultCriteriaTest {

    @Test
    void newAnalysisResultCriteriaHasAllFiltersNullTest() {
        var analysisResultCriteria = new AnalysisResultCriteria();
        assertThat(analysisResultCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void analysisResultCriteriaFluentMethodsCreatesFiltersTest() {
        var analysisResultCriteria = new AnalysisResultCriteria();

        setAllFilters(analysisResultCriteria);

        assertThat(analysisResultCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void analysisResultCriteriaCopyCreatesNullFilterTest() {
        var analysisResultCriteria = new AnalysisResultCriteria();
        var copy = analysisResultCriteria.copy();

        assertThat(analysisResultCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(analysisResultCriteria)
        );
    }

    @Test
    void analysisResultCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var analysisResultCriteria = new AnalysisResultCriteria();
        setAllFilters(analysisResultCriteria);

        var copy = analysisResultCriteria.copy();

        assertThat(analysisResultCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(analysisResultCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var analysisResultCriteria = new AnalysisResultCriteria();

        assertThat(analysisResultCriteria).hasToString("AnalysisResultCriteria{}");
    }

    private static void setAllFilters(AnalysisResultCriteria analysisResultCriteria) {
        analysisResultCriteria.id();
        analysisResultCriteria.summary();
        analysisResultCriteria.sentiment();
        analysisResultCriteria.topic();
        analysisResultCriteria.entities();
        analysisResultCriteria.riskSource();
        analysisResultCriteria.status();
        analysisResultCriteria.modelName();
        analysisResultCriteria.analyzedAt();
        analysisResultCriteria.errorMessage();
        analysisResultCriteria.newsItemId();
        analysisResultCriteria.distinct();
    }

    private static Condition<AnalysisResultCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getSummary()) &&
                condition.apply(criteria.getSentiment()) &&
                condition.apply(criteria.getTopic()) &&
                condition.apply(criteria.getEntities()) &&
                condition.apply(criteria.getRiskSource()) &&
                condition.apply(criteria.getStatus()) &&
                condition.apply(criteria.getModelName()) &&
                condition.apply(criteria.getAnalyzedAt()) &&
                condition.apply(criteria.getErrorMessage()) &&
                condition.apply(criteria.getNewsItemId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<AnalysisResultCriteria> copyFiltersAre(
        AnalysisResultCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getSummary(), copy.getSummary()) &&
                condition.apply(criteria.getSentiment(), copy.getSentiment()) &&
                condition.apply(criteria.getTopic(), copy.getTopic()) &&
                condition.apply(criteria.getEntities(), copy.getEntities()) &&
                condition.apply(criteria.getRiskSource(), copy.getRiskSource()) &&
                condition.apply(criteria.getStatus(), copy.getStatus()) &&
                condition.apply(criteria.getModelName(), copy.getModelName()) &&
                condition.apply(criteria.getAnalyzedAt(), copy.getAnalyzedAt()) &&
                condition.apply(criteria.getErrorMessage(), copy.getErrorMessage()) &&
                condition.apply(criteria.getNewsItemId(), copy.getNewsItemId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
