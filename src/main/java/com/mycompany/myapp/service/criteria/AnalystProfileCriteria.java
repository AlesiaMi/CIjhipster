package com.mycompany.myapp.service.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.mycompany.myapp.domain.AnalystProfile} entity. This class is used
 * in {@link com.mycompany.myapp.web.rest.AnalystProfileResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /analyst-profiles?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AnalystProfileCriteria implements Serializable, Criteria {

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter displayName;

    private StringFilter telegramChatId;

    private BooleanFilter notificationEnabled;

    private InstantFilter createdAt;

    private LongFilter userId;

    private LongFilter competitorsId;

    private Boolean distinct;

    public AnalystProfileCriteria() {}

    public AnalystProfileCriteria(AnalystProfileCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.displayName = other.optionalDisplayName().map(StringFilter::copy).orElse(null);
        this.telegramChatId = other.optionalTelegramChatId().map(StringFilter::copy).orElse(null);
        this.notificationEnabled = other.optionalNotificationEnabled().map(BooleanFilter::copy).orElse(null);
        this.createdAt = other.optionalCreatedAt().map(InstantFilter::copy).orElse(null);
        this.userId = other.optionalUserId().map(LongFilter::copy).orElse(null);
        this.competitorsId = other.optionalCompetitorsId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public AnalystProfileCriteria copy() {
        return new AnalystProfileCriteria(this);
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

    public StringFilter getDisplayName() {
        return displayName;
    }

    public Optional<StringFilter> optionalDisplayName() {
        return Optional.ofNullable(displayName);
    }

    public StringFilter displayName() {
        if (displayName == null) {
            setDisplayName(new StringFilter());
        }
        return displayName;
    }

    public void setDisplayName(StringFilter displayName) {
        this.displayName = displayName;
    }

    public StringFilter getTelegramChatId() {
        return telegramChatId;
    }

    public Optional<StringFilter> optionalTelegramChatId() {
        return Optional.ofNullable(telegramChatId);
    }

    public StringFilter telegramChatId() {
        if (telegramChatId == null) {
            setTelegramChatId(new StringFilter());
        }
        return telegramChatId;
    }

    public void setTelegramChatId(StringFilter telegramChatId) {
        this.telegramChatId = telegramChatId;
    }

    public BooleanFilter getNotificationEnabled() {
        return notificationEnabled;
    }

    public Optional<BooleanFilter> optionalNotificationEnabled() {
        return Optional.ofNullable(notificationEnabled);
    }

    public BooleanFilter notificationEnabled() {
        if (notificationEnabled == null) {
            setNotificationEnabled(new BooleanFilter());
        }
        return notificationEnabled;
    }

    public void setNotificationEnabled(BooleanFilter notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
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

    public LongFilter getUserId() {
        return userId;
    }

    public Optional<LongFilter> optionalUserId() {
        return Optional.ofNullable(userId);
    }

    public LongFilter userId() {
        if (userId == null) {
            setUserId(new LongFilter());
        }
        return userId;
    }

    public void setUserId(LongFilter userId) {
        this.userId = userId;
    }

    public LongFilter getCompetitorsId() {
        return competitorsId;
    }

    public Optional<LongFilter> optionalCompetitorsId() {
        return Optional.ofNullable(competitorsId);
    }

    public LongFilter competitorsId() {
        if (competitorsId == null) {
            setCompetitorsId(new LongFilter());
        }
        return competitorsId;
    }

    public void setCompetitorsId(LongFilter competitorsId) {
        this.competitorsId = competitorsId;
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
        final AnalystProfileCriteria that = (AnalystProfileCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(displayName, that.displayName) &&
            Objects.equals(telegramChatId, that.telegramChatId) &&
            Objects.equals(notificationEnabled, that.notificationEnabled) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(userId, that.userId) &&
            Objects.equals(competitorsId, that.competitorsId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, telegramChatId, notificationEnabled, createdAt, userId, competitorsId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AnalystProfileCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalDisplayName().map(f -> "displayName=" + f + ", ").orElse("") +
            optionalTelegramChatId().map(f -> "telegramChatId=" + f + ", ").orElse("") +
            optionalNotificationEnabled().map(f -> "notificationEnabled=" + f + ", ").orElse("") +
            optionalCreatedAt().map(f -> "createdAt=" + f + ", ").orElse("") +
            optionalUserId().map(f -> "userId=" + f + ", ").orElse("") +
            optionalCompetitorsId().map(f -> "competitorsId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
