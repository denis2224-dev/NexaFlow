package com.nexaflow.notification.service.criteria;

import com.nexaflow.notification.domain.enumeration.NotificationType;
import com.nexaflow.notification.domain.enumeration.SourceType;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.nexaflow.notification.domain.Notification} entity. This class is used
 * in {@link com.nexaflow.notification.web.rest.NotificationResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /notifications?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class NotificationCriteria implements Serializable, Criteria {

    /**
     * Class for filtering NotificationType
     */
    public static class NotificationTypeFilter extends Filter<NotificationType> {

        public NotificationTypeFilter() {}

        public NotificationTypeFilter(NotificationTypeFilter filter) {
            super(filter);
        }

        @Override
        public NotificationTypeFilter copy() {
            return new NotificationTypeFilter(this);
        }
    }

    /**
     * Class for filtering SourceType
     */
    public static class SourceTypeFilter extends Filter<SourceType> {

        public SourceTypeFilter() {}

        public SourceTypeFilter(SourceTypeFilter filter) {
            super(filter);
        }

        @Override
        public SourceTypeFilter copy() {
            return new SourceTypeFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private LongFilter organizationId;

    private StringFilter recipientLogin;

    private StringFilter title;

    private NotificationTypeFilter type;

    private SourceTypeFilter sourceType;

    private LongFilter sourceId;

    private BooleanFilter isRead;

    private InstantFilter createdAt;

    private Boolean distinct;

    public NotificationCriteria() {}

    public NotificationCriteria(NotificationCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.organizationId = other.optionalOrganizationId().map(LongFilter::copy).orElse(null);
        this.recipientLogin = other.optionalRecipientLogin().map(StringFilter::copy).orElse(null);
        this.title = other.optionalTitle().map(StringFilter::copy).orElse(null);
        this.type = other.optionalType().map(NotificationTypeFilter::copy).orElse(null);
        this.sourceType = other.optionalSourceType().map(SourceTypeFilter::copy).orElse(null);
        this.sourceId = other.optionalSourceId().map(LongFilter::copy).orElse(null);
        this.isRead = other.optionalIsRead().map(BooleanFilter::copy).orElse(null);
        this.createdAt = other.optionalCreatedAt().map(InstantFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public NotificationCriteria copy() {
        return new NotificationCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public Optional<LongFilter> optionalId() {
        return Optional.ofNullable(id);
    }

    public LongFilter id() {
        if (id == null) {
            setId(new LongFilter());
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public LongFilter getOrganizationId() {
        return organizationId;
    }

    public Optional<LongFilter> optionalOrganizationId() {
        return Optional.ofNullable(organizationId);
    }

    public LongFilter organizationId() {
        if (organizationId == null) {
            setOrganizationId(new LongFilter());
        }
        return organizationId;
    }

    public void setOrganizationId(LongFilter organizationId) {
        this.organizationId = organizationId;
    }

    public StringFilter getRecipientLogin() {
        return recipientLogin;
    }

    public Optional<StringFilter> optionalRecipientLogin() {
        return Optional.ofNullable(recipientLogin);
    }

    public StringFilter recipientLogin() {
        if (recipientLogin == null) {
            setRecipientLogin(new StringFilter());
        }
        return recipientLogin;
    }

    public void setRecipientLogin(StringFilter recipientLogin) {
        this.recipientLogin = recipientLogin;
    }

    public StringFilter getTitle() {
        return title;
    }

    public Optional<StringFilter> optionalTitle() {
        return Optional.ofNullable(title);
    }

    public StringFilter title() {
        if (title == null) {
            setTitle(new StringFilter());
        }
        return title;
    }

    public void setTitle(StringFilter title) {
        this.title = title;
    }

    public NotificationTypeFilter getType() {
        return type;
    }

    public Optional<NotificationTypeFilter> optionalType() {
        return Optional.ofNullable(type);
    }

    public NotificationTypeFilter type() {
        if (type == null) {
            setType(new NotificationTypeFilter());
        }
        return type;
    }

    public void setType(NotificationTypeFilter type) {
        this.type = type;
    }

    public SourceTypeFilter getSourceType() {
        return sourceType;
    }

    public Optional<SourceTypeFilter> optionalSourceType() {
        return Optional.ofNullable(sourceType);
    }

    public SourceTypeFilter sourceType() {
        if (sourceType == null) {
            setSourceType(new SourceTypeFilter());
        }
        return sourceType;
    }

    public void setSourceType(SourceTypeFilter sourceType) {
        this.sourceType = sourceType;
    }

    public LongFilter getSourceId() {
        return sourceId;
    }

    public Optional<LongFilter> optionalSourceId() {
        return Optional.ofNullable(sourceId);
    }

    public LongFilter sourceId() {
        if (sourceId == null) {
            setSourceId(new LongFilter());
        }
        return sourceId;
    }

    public void setSourceId(LongFilter sourceId) {
        this.sourceId = sourceId;
    }

    public BooleanFilter getIsRead() {
        return isRead;
    }

    public Optional<BooleanFilter> optionalIsRead() {
        return Optional.ofNullable(isRead);
    }

    public BooleanFilter isRead() {
        if (isRead == null) {
            setIsRead(new BooleanFilter());
        }
        return isRead;
    }

    public void setIsRead(BooleanFilter isRead) {
        this.isRead = isRead;
    }

    public InstantFilter getCreatedAt() {
        return createdAt;
    }

    public Optional<InstantFilter> optionalCreatedAt() {
        return Optional.ofNullable(createdAt);
    }

    public InstantFilter createdAt() {
        if (createdAt == null) {
            setCreatedAt(new InstantFilter());
        }
        return createdAt;
    }

    public void setCreatedAt(InstantFilter createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public Optional<Boolean> optionalDistinct() {
        return Optional.ofNullable(distinct);
    }

    public Boolean distinct() {
        if (distinct == null) {
            setDistinct(true);
        }
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final NotificationCriteria that = (NotificationCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(organizationId, that.organizationId) &&
            Objects.equals(recipientLogin, that.recipientLogin) &&
            Objects.equals(title, that.title) &&
            Objects.equals(type, that.type) &&
            Objects.equals(sourceType, that.sourceType) &&
            Objects.equals(sourceId, that.sourceId) &&
            Objects.equals(isRead, that.isRead) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, organizationId, recipientLogin, title, type, sourceType, sourceId, isRead, createdAt, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "NotificationCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalOrganizationId().map(f -> "organizationId=" + f + ", ").orElse("") +
            optionalRecipientLogin().map(f -> "recipientLogin=" + f + ", ").orElse("") +
            optionalTitle().map(f -> "title=" + f + ", ").orElse("") +
            optionalType().map(f -> "type=" + f + ", ").orElse("") +
            optionalSourceType().map(f -> "sourceType=" + f + ", ").orElse("") +
            optionalSourceId().map(f -> "sourceId=" + f + ", ").orElse("") +
            optionalIsRead().map(f -> "isRead=" + f + ", ").orElse("") +
            optionalCreatedAt().map(f -> "createdAt=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
