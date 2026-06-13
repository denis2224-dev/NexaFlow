package com.nexaflow.billing.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class SubscriptionCriteriaTest {

    @Test
    void newSubscriptionCriteriaHasAllFiltersNullTest() {
        var subscriptionCriteria = new SubscriptionCriteria();
        assertThat(subscriptionCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void subscriptionCriteriaFluentMethodsCreatesFiltersTest() {
        var subscriptionCriteria = new SubscriptionCriteria();

        setAllFilters(subscriptionCriteria);

        assertThat(subscriptionCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void subscriptionCriteriaCopyCreatesNullFilterTest() {
        var subscriptionCriteria = new SubscriptionCriteria();
        var copy = subscriptionCriteria.copy();

        assertThat(subscriptionCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(subscriptionCriteria)
        );
    }

    @Test
    void subscriptionCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var subscriptionCriteria = new SubscriptionCriteria();
        setAllFilters(subscriptionCriteria);

        var copy = subscriptionCriteria.copy();

        assertThat(subscriptionCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(subscriptionCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var subscriptionCriteria = new SubscriptionCriteria();

        assertThat(subscriptionCriteria).hasToString("SubscriptionCriteria{}");
    }

    private static void setAllFilters(SubscriptionCriteria subscriptionCriteria) {
        subscriptionCriteria.id();
        subscriptionCriteria.organizationId();
        subscriptionCriteria.planCode();
        subscriptionCriteria.status();
        subscriptionCriteria.startedAt();
        subscriptionCriteria.expiresAt();
        subscriptionCriteria.createdBy();
        subscriptionCriteria.createdAt();
        subscriptionCriteria.updatedAt();
        subscriptionCriteria.distinct();
    }

    private static Condition<SubscriptionCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getOrganizationId()) &&
                condition.apply(criteria.getPlanCode()) &&
                condition.apply(criteria.getStatus()) &&
                condition.apply(criteria.getStartedAt()) &&
                condition.apply(criteria.getExpiresAt()) &&
                condition.apply(criteria.getCreatedBy()) &&
                condition.apply(criteria.getCreatedAt()) &&
                condition.apply(criteria.getUpdatedAt()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<SubscriptionCriteria> copyFiltersAre(
        SubscriptionCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getOrganizationId(), copy.getOrganizationId()) &&
                condition.apply(criteria.getPlanCode(), copy.getPlanCode()) &&
                condition.apply(criteria.getStatus(), copy.getStatus()) &&
                condition.apply(criteria.getStartedAt(), copy.getStartedAt()) &&
                condition.apply(criteria.getExpiresAt(), copy.getExpiresAt()) &&
                condition.apply(criteria.getCreatedBy(), copy.getCreatedBy()) &&
                condition.apply(criteria.getCreatedAt(), copy.getCreatedAt()) &&
                condition.apply(criteria.getUpdatedAt(), copy.getUpdatedAt()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
