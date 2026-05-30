package com.nexaflow.userservice.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class MembershipCriteriaTest {

    @Test
    void newMembershipCriteriaHasAllFiltersNullTest() {
        var membershipCriteria = new MembershipCriteria();
        assertThat(membershipCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void membershipCriteriaFluentMethodsCreatesFiltersTest() {
        var membershipCriteria = new MembershipCriteria();

        setAllFilters(membershipCriteria);

        assertThat(membershipCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void membershipCriteriaCopyCreatesNullFilterTest() {
        var membershipCriteria = new MembershipCriteria();
        var copy = membershipCriteria.copy();

        assertThat(membershipCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(membershipCriteria)
        );
    }

    @Test
    void membershipCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var membershipCriteria = new MembershipCriteria();
        setAllFilters(membershipCriteria);

        var copy = membershipCriteria.copy();

        assertThat(membershipCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(membershipCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var membershipCriteria = new MembershipCriteria();

        assertThat(membershipCriteria).hasToString("MembershipCriteria{}");
    }

    private static void setAllFilters(MembershipCriteria membershipCriteria) {
        membershipCriteria.id();
        membershipCriteria.userId();
        membershipCriteria.userLogin();
        membershipCriteria.userEmail();
        membershipCriteria.role();
        membershipCriteria.joinedAt();
        membershipCriteria.active();
        membershipCriteria.organizationId();
        membershipCriteria.distinct();
    }

    private static Condition<MembershipCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getUserId()) &&
                condition.apply(criteria.getUserLogin()) &&
                condition.apply(criteria.getUserEmail()) &&
                condition.apply(criteria.getRole()) &&
                condition.apply(criteria.getJoinedAt()) &&
                condition.apply(criteria.getActive()) &&
                condition.apply(criteria.getOrganizationId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<MembershipCriteria> copyFiltersAre(MembershipCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getUserId(), copy.getUserId()) &&
                condition.apply(criteria.getUserLogin(), copy.getUserLogin()) &&
                condition.apply(criteria.getUserEmail(), copy.getUserEmail()) &&
                condition.apply(criteria.getRole(), copy.getRole()) &&
                condition.apply(criteria.getJoinedAt(), copy.getJoinedAt()) &&
                condition.apply(criteria.getActive(), copy.getActive()) &&
                condition.apply(criteria.getOrganizationId(), copy.getOrganizationId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
