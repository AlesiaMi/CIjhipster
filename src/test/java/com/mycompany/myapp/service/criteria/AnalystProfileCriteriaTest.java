package com.mycompany.myapp.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class AnalystProfileCriteriaTest {

    @Test
    void newAnalystProfileCriteriaHasAllFiltersNullTest() {
        var analystProfileCriteria = new AnalystProfileCriteria();
        assertThat(analystProfileCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void analystProfileCriteriaFluentMethodsCreatesFiltersTest() {
        var analystProfileCriteria = new AnalystProfileCriteria();

        setAllFilters(analystProfileCriteria);

        assertThat(analystProfileCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void analystProfileCriteriaCopyCreatesNullFilterTest() {
        var analystProfileCriteria = new AnalystProfileCriteria();
        var copy = analystProfileCriteria.copy();

        assertThat(analystProfileCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(analystProfileCriteria)
        );
    }

    @Test
    void analystProfileCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var analystProfileCriteria = new AnalystProfileCriteria();
        setAllFilters(analystProfileCriteria);

        var copy = analystProfileCriteria.copy();

        assertThat(analystProfileCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(analystProfileCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var analystProfileCriteria = new AnalystProfileCriteria();

        assertThat(analystProfileCriteria).hasToString("AnalystProfileCriteria{}");
    }

    private static void setAllFilters(AnalystProfileCriteria analystProfileCriteria) {
        analystProfileCriteria.id();
        analystProfileCriteria.displayName();
        analystProfileCriteria.telegramChatId();
        analystProfileCriteria.notificationEnabled();
        analystProfileCriteria.createdAt();
        analystProfileCriteria.userId();
        analystProfileCriteria.competitorsId();
        analystProfileCriteria.distinct();
    }

    private static Condition<AnalystProfileCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getDisplayName()) &&
                condition.apply(criteria.getTelegramChatId()) &&
                condition.apply(criteria.getNotificationEnabled()) &&
                condition.apply(criteria.getCreatedAt()) &&
                condition.apply(criteria.getUserId()) &&
                condition.apply(criteria.getCompetitorsId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<AnalystProfileCriteria> copyFiltersAre(
        AnalystProfileCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getDisplayName(), copy.getDisplayName()) &&
                condition.apply(criteria.getTelegramChatId(), copy.getTelegramChatId()) &&
                condition.apply(criteria.getNotificationEnabled(), copy.getNotificationEnabled()) &&
                condition.apply(criteria.getCreatedAt(), copy.getCreatedAt()) &&
                condition.apply(criteria.getUserId(), copy.getUserId()) &&
                condition.apply(criteria.getCompetitorsId(), copy.getCompetitorsId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
