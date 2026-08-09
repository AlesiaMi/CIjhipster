package com.mycompany.myapp.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class CompetitorCriteriaTest {

    @Test
    void newCompetitorCriteriaHasAllFiltersNullTest() {
        var competitorCriteria = new CompetitorCriteria();
        assertThat(competitorCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void competitorCriteriaFluentMethodsCreatesFiltersTest() {
        var competitorCriteria = new CompetitorCriteria();

        setAllFilters(competitorCriteria);

        assertThat(competitorCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void competitorCriteriaCopyCreatesNullFilterTest() {
        var competitorCriteria = new CompetitorCriteria();
        var copy = competitorCriteria.copy();

        assertThat(competitorCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(competitorCriteria)
        );
    }

    @Test
    void competitorCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var competitorCriteria = new CompetitorCriteria();
        setAllFilters(competitorCriteria);

        var copy = competitorCriteria.copy();

        assertThat(competitorCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(competitorCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var competitorCriteria = new CompetitorCriteria();

        assertThat(competitorCriteria).hasToString("CompetitorCriteria{}");
    }

    private static void setAllFilters(CompetitorCriteria competitorCriteria) {
        competitorCriteria.id();
        competitorCriteria.competitorName();
        competitorCriteria.websiteUrl();
        competitorCriteria.industry();
        competitorCriteria.description();
        competitorCriteria.isActive();
        competitorCriteria.analystProfilesId();
        competitorCriteria.distinct();
    }

    private static Condition<CompetitorCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getCompetitorName()) &&
                condition.apply(criteria.getWebsiteUrl()) &&
                condition.apply(criteria.getIndustry()) &&
                condition.apply(criteria.getDescription()) &&
                condition.apply(criteria.getIsActive()) &&
                condition.apply(criteria.getAnalystProfilesId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<CompetitorCriteria> copyFiltersAre(CompetitorCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getCompetitorName(), copy.getCompetitorName()) &&
                condition.apply(criteria.getWebsiteUrl(), copy.getWebsiteUrl()) &&
                condition.apply(criteria.getIndustry(), copy.getIndustry()) &&
                condition.apply(criteria.getDescription(), copy.getDescription()) &&
                condition.apply(criteria.getIsActive(), copy.getIsActive()) &&
                condition.apply(criteria.getAnalystProfilesId(), copy.getAnalystProfilesId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
