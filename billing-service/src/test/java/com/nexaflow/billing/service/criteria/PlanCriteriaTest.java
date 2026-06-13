package com.nexaflow.billing.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class PlanCriteriaTest {

    @Test
    void newPlanCriteriaHasAllFiltersNullTest() {
        var planCriteria = new PlanCriteria();
        assertThat(planCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void planCriteriaFluentMethodsCreatesFiltersTest() {
        var planCriteria = new PlanCriteria();

        setAllFilters(planCriteria);

        assertThat(planCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void planCriteriaCopyCreatesNullFilterTest() {
        var planCriteria = new PlanCriteria();
        var copy = planCriteria.copy();

        assertThat(planCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(planCriteria)
        );
    }

    @Test
    void planCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var planCriteria = new PlanCriteria();
        setAllFilters(planCriteria);

        var copy = planCriteria.copy();

        assertThat(planCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(planCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var planCriteria = new PlanCriteria();

        assertThat(planCriteria).hasToString("PlanCriteria{}");
    }

    private static void setAllFilters(PlanCriteria planCriteria) {
        planCriteria.id();
        planCriteria.code();
        planCriteria.name();
        planCriteria.priceMonthly();
        planCriteria.maxProjects();
        planCriteria.maxUsers();
        planCriteria.maxTasks();
        planCriteria.active();
        planCriteria.createdAt();
        planCriteria.updatedAt();
        planCriteria.distinct();
    }

    private static Condition<PlanCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getCode()) &&
                condition.apply(criteria.getName()) &&
                condition.apply(criteria.getPriceMonthly()) &&
                condition.apply(criteria.getMaxProjects()) &&
                condition.apply(criteria.getMaxUsers()) &&
                condition.apply(criteria.getMaxTasks()) &&
                condition.apply(criteria.getActive()) &&
                condition.apply(criteria.getCreatedAt()) &&
                condition.apply(criteria.getUpdatedAt()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<PlanCriteria> copyFiltersAre(PlanCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getCode(), copy.getCode()) &&
                condition.apply(criteria.getName(), copy.getName()) &&
                condition.apply(criteria.getPriceMonthly(), copy.getPriceMonthly()) &&
                condition.apply(criteria.getMaxProjects(), copy.getMaxProjects()) &&
                condition.apply(criteria.getMaxUsers(), copy.getMaxUsers()) &&
                condition.apply(criteria.getMaxTasks(), copy.getMaxTasks()) &&
                condition.apply(criteria.getActive(), copy.getActive()) &&
                condition.apply(criteria.getCreatedAt(), copy.getCreatedAt()) &&
                condition.apply(criteria.getUpdatedAt(), copy.getUpdatedAt()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
