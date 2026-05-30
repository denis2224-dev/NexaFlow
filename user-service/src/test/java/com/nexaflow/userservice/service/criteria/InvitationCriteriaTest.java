package com.nexaflow.userservice.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class InvitationCriteriaTest {

    @Test
    void newInvitationCriteriaHasAllFiltersNullTest() {
        var invitationCriteria = new InvitationCriteria();
        assertThat(invitationCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void invitationCriteriaFluentMethodsCreatesFiltersTest() {
        var invitationCriteria = new InvitationCriteria();

        setAllFilters(invitationCriteria);

        assertThat(invitationCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void invitationCriteriaCopyCreatesNullFilterTest() {
        var invitationCriteria = new InvitationCriteria();
        var copy = invitationCriteria.copy();

        assertThat(invitationCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(invitationCriteria)
        );
    }

    @Test
    void invitationCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var invitationCriteria = new InvitationCriteria();
        setAllFilters(invitationCriteria);

        var copy = invitationCriteria.copy();

        assertThat(invitationCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(invitationCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var invitationCriteria = new InvitationCriteria();

        assertThat(invitationCriteria).hasToString("InvitationCriteria{}");
    }

    private static void setAllFilters(InvitationCriteria invitationCriteria) {
        invitationCriteria.id();
        invitationCriteria.email();
        invitationCriteria.token();
        invitationCriteria.role();
        invitationCriteria.status();
        invitationCriteria.invitedAt();
        invitationCriteria.expiresAt();
        invitationCriteria.acceptedAt();
        invitationCriteria.invitedByUserId();
        invitationCriteria.invitedByLogin();
        invitationCriteria.organizationId();
        invitationCriteria.distinct();
    }

    private static Condition<InvitationCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getEmail()) &&
                condition.apply(criteria.getToken()) &&
                condition.apply(criteria.getRole()) &&
                condition.apply(criteria.getStatus()) &&
                condition.apply(criteria.getInvitedAt()) &&
                condition.apply(criteria.getExpiresAt()) &&
                condition.apply(criteria.getAcceptedAt()) &&
                condition.apply(criteria.getInvitedByUserId()) &&
                condition.apply(criteria.getInvitedByLogin()) &&
                condition.apply(criteria.getOrganizationId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<InvitationCriteria> copyFiltersAre(InvitationCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getEmail(), copy.getEmail()) &&
                condition.apply(criteria.getToken(), copy.getToken()) &&
                condition.apply(criteria.getRole(), copy.getRole()) &&
                condition.apply(criteria.getStatus(), copy.getStatus()) &&
                condition.apply(criteria.getInvitedAt(), copy.getInvitedAt()) &&
                condition.apply(criteria.getExpiresAt(), copy.getExpiresAt()) &&
                condition.apply(criteria.getAcceptedAt(), copy.getAcceptedAt()) &&
                condition.apply(criteria.getInvitedByUserId(), copy.getInvitedByUserId()) &&
                condition.apply(criteria.getInvitedByLogin(), copy.getInvitedByLogin()) &&
                condition.apply(criteria.getOrganizationId(), copy.getOrganizationId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
