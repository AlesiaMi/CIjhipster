package com.mycompany.myapp.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class CiAlertCriteriaTest {

    @Test
    void newCiAlertCriteriaHasAllFiltersNullTest() {
        var ciAlertCriteria = new CiAlertCriteria();
        assertThat(ciAlertCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void ciAlertCriteriaFluentMethodsCreatesFiltersTest() {
        var ciAlertCriteria = new CiAlertCriteria();

        setAllFilters(ciAlertCriteria);

        assertThat(ciAlertCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void ciAlertCriteriaCopyCreatesNullFilterTest() {
        var ciAlertCriteria = new CiAlertCriteria();
        var copy = ciAlertCriteria.copy();

        assertThat(ciAlertCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(ciAlertCriteria)
        );
    }

    @Test
    void ciAlertCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var ciAlertCriteria = new CiAlertCriteria();
        setAllFilters(ciAlertCriteria);

        var copy = ciAlertCriteria.copy();

        assertThat(ciAlertCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(ciAlertCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var ciAlertCriteria = new CiAlertCriteria();

        assertThat(ciAlertCriteria).hasToString("CiAlertCriteria{}");
    }

    private static void setAllFilters(CiAlertCriteria ciAlertCriteria) {
        ciAlertCriteria.id();
        ciAlertCriteria.title();
        ciAlertCriteria.message();
        ciAlertCriteria.severity();
        ciAlertCriteria.status();
        ciAlertCriteria.createdAt();
        ciAlertCriteria.readAt();
        ciAlertCriteria.analysisResultId();
        ciAlertCriteria.analystProfileId();
        ciAlertCriteria.distinct();
    }

    private static Condition<CiAlertCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getTitle()) &&
                condition.apply(criteria.getMessage()) &&
                condition.apply(criteria.getSeverity()) &&
                condition.apply(criteria.getStatus()) &&
                condition.apply(criteria.getCreatedAt()) &&
                condition.apply(criteria.getReadAt()) &&
                condition.apply(criteria.getAnalysisResultId()) &&
                condition.apply(criteria.getAnalystProfileId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<CiAlertCriteria> copyFiltersAre(CiAlertCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getTitle(), copy.getTitle()) &&
                condition.apply(criteria.getMessage(), copy.getMessage()) &&
                condition.apply(criteria.getSeverity(), copy.getSeverity()) &&
                condition.apply(criteria.getStatus(), copy.getStatus()) &&
                condition.apply(criteria.getCreatedAt(), copy.getCreatedAt()) &&
                condition.apply(criteria.getReadAt(), copy.getReadAt()) &&
                condition.apply(criteria.getAnalysisResultId(), copy.getAnalysisResultId()) &&
                condition.apply(criteria.getAnalystProfileId(), copy.getAnalystProfileId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
