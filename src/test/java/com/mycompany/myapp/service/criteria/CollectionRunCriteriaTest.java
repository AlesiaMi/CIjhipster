package com.mycompany.myapp.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class CollectionRunCriteriaTest {

    @Test
    void newCollectionRunCriteriaHasAllFiltersNullTest() {
        var collectionRunCriteria = new CollectionRunCriteria();
        assertThat(collectionRunCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void collectionRunCriteriaFluentMethodsCreatesFiltersTest() {
        var collectionRunCriteria = new CollectionRunCriteria();

        setAllFilters(collectionRunCriteria);

        assertThat(collectionRunCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void collectionRunCriteriaCopyCreatesNullFilterTest() {
        var collectionRunCriteria = new CollectionRunCriteria();
        var copy = collectionRunCriteria.copy();

        assertThat(collectionRunCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(collectionRunCriteria)
        );
    }

    @Test
    void collectionRunCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var collectionRunCriteria = new CollectionRunCriteria();
        setAllFilters(collectionRunCriteria);

        var copy = collectionRunCriteria.copy();

        assertThat(collectionRunCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(collectionRunCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var collectionRunCriteria = new CollectionRunCriteria();

        assertThat(collectionRunCriteria).hasToString("CollectionRunCriteria{}");
    }

    private static void setAllFilters(CollectionRunCriteria collectionRunCriteria) {
        collectionRunCriteria.id();
        collectionRunCriteria.startedAt();
        collectionRunCriteria.finishedAt();
        collectionRunCriteria.status();
        collectionRunCriteria.foundCount();
        collectionRunCriteria.processedCount();
        collectionRunCriteria.errorMessage();
        collectionRunCriteria.distinct();
    }

    private static Condition<CollectionRunCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getStartedAt()) &&
                condition.apply(criteria.getFinishedAt()) &&
                condition.apply(criteria.getStatus()) &&
                condition.apply(criteria.getFoundCount()) &&
                condition.apply(criteria.getProcessedCount()) &&
                condition.apply(criteria.getErrorMessage()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<CollectionRunCriteria> copyFiltersAre(
        CollectionRunCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getStartedAt(), copy.getStartedAt()) &&
                condition.apply(criteria.getFinishedAt(), copy.getFinishedAt()) &&
                condition.apply(criteria.getStatus(), copy.getStatus()) &&
                condition.apply(criteria.getFoundCount(), copy.getFoundCount()) &&
                condition.apply(criteria.getProcessedCount(), copy.getProcessedCount()) &&
                condition.apply(criteria.getErrorMessage(), copy.getErrorMessage()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
