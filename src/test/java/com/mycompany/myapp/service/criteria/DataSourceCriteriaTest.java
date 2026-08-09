package com.mycompany.myapp.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class DataSourceCriteriaTest {

    @Test
    void newDataSourceCriteriaHasAllFiltersNullTest() {
        var dataSourceCriteria = new DataSourceCriteria();
        assertThat(dataSourceCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void dataSourceCriteriaFluentMethodsCreatesFiltersTest() {
        var dataSourceCriteria = new DataSourceCriteria();

        setAllFilters(dataSourceCriteria);

        assertThat(dataSourceCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void dataSourceCriteriaCopyCreatesNullFilterTest() {
        var dataSourceCriteria = new DataSourceCriteria();
        var copy = dataSourceCriteria.copy();

        assertThat(dataSourceCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(dataSourceCriteria)
        );
    }

    @Test
    void dataSourceCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var dataSourceCriteria = new DataSourceCriteria();
        setAllFilters(dataSourceCriteria);

        var copy = dataSourceCriteria.copy();

        assertThat(dataSourceCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(dataSourceCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var dataSourceCriteria = new DataSourceCriteria();

        assertThat(dataSourceCriteria).hasToString("DataSourceCriteria{}");
    }

    private static void setAllFilters(DataSourceCriteria dataSourceCriteria) {
        dataSourceCriteria.id();
        dataSourceCriteria.sourceName();
        dataSourceCriteria.url();
        dataSourceCriteria.sourceType();
        dataSourceCriteria.isActive();
        dataSourceCriteria.lastCheckedAt();
        dataSourceCriteria.createdAt();
        dataSourceCriteria.competitorId();
        dataSourceCriteria.distinct();
    }

    private static Condition<DataSourceCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getSourceName()) &&
                condition.apply(criteria.getUrl()) &&
                condition.apply(criteria.getSourceType()) &&
                condition.apply(criteria.getIsActive()) &&
                condition.apply(criteria.getLastCheckedAt()) &&
                condition.apply(criteria.getCreatedAt()) &&
                condition.apply(criteria.getCompetitorId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<DataSourceCriteria> copyFiltersAre(DataSourceCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getSourceName(), copy.getSourceName()) &&
                condition.apply(criteria.getUrl(), copy.getUrl()) &&
                condition.apply(criteria.getSourceType(), copy.getSourceType()) &&
                condition.apply(criteria.getIsActive(), copy.getIsActive()) &&
                condition.apply(criteria.getLastCheckedAt(), copy.getLastCheckedAt()) &&
                condition.apply(criteria.getCreatedAt(), copy.getCreatedAt()) &&
                condition.apply(criteria.getCompetitorId(), copy.getCompetitorId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
