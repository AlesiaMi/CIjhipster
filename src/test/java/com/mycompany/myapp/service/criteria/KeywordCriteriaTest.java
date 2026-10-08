package com.mycompany.myapp.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class KeywordCriteriaTest {

    @Test
    void newKeywordCriteriaHasAllFiltersNullTest() {
        var keywordCriteria = new KeywordCriteria();
        assertThat(keywordCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void keywordCriteriaFluentMethodsCreatesFiltersTest() {
        var keywordCriteria = new KeywordCriteria();

        setAllFilters(keywordCriteria);

        assertThat(keywordCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void keywordCriteriaCopyCreatesNullFilterTest() {
        var keywordCriteria = new KeywordCriteria();
        var copy = keywordCriteria.copy();

        assertThat(keywordCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(keywordCriteria)
        );
    }

    @Test
    void keywordCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var keywordCriteria = new KeywordCriteria();
        setAllFilters(keywordCriteria);

        var copy = keywordCriteria.copy();

        assertThat(keywordCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(keywordCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var keywordCriteria = new KeywordCriteria();

        assertThat(keywordCriteria).hasToString("KeywordCriteria{}");
    }

    private static void setAllFilters(KeywordCriteria keywordCriteria) {
        keywordCriteria.id();
        keywordCriteria.value();
        keywordCriteria.isActive();
        keywordCriteria.createdAt();
        keywordCriteria.competitorId();
        keywordCriteria.distinct();
    }

    private static Condition<KeywordCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getValue()) &&
                condition.apply(criteria.getIsActive()) &&
                condition.apply(criteria.getCreatedAt()) &&
                condition.apply(criteria.getCompetitorId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<KeywordCriteria> copyFiltersAre(KeywordCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getValue(), copy.getValue()) &&
                condition.apply(criteria.getIsActive(), copy.getIsActive()) &&
                condition.apply(criteria.getCreatedAt(), copy.getCreatedAt()) &&
                condition.apply(criteria.getCompetitorId(), copy.getCompetitorId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
