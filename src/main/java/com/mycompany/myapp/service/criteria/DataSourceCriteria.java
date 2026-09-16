package com.mycompany.myapp.service.criteria;

import com.mycompany.myapp.domain.enumeration.SourceType;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.mycompany.myapp.domain.DataSource} entity. This class is used
 * in {@link com.mycompany.myapp.web.rest.DataSourceResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /data-sources?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DataSourceCriteria implements Serializable, Criteria {

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

    private StringFilter sourceName;

    private StringFilter url;

    private SourceTypeFilter sourceType;

    private BooleanFilter isActive;

    private InstantFilter lastCheckedAt;

    private InstantFilter createdAt;

    private LongFilter competitorId;

    private LongFilter ownerId;

    private Boolean distinct;

    public DataSourceCriteria() {}

    public DataSourceCriteria(DataSourceCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.sourceName = other.optionalSourceName().map(StringFilter::copy).orElse(null);
        this.url = other.optionalUrl().map(StringFilter::copy).orElse(null);
        this.sourceType = other.optionalSourceType().map(SourceTypeFilter::copy).orElse(null);
        this.isActive = other.optionalIsActive().map(BooleanFilter::copy).orElse(null);
        this.lastCheckedAt = other.optionalLastCheckedAt().map(InstantFilter::copy).orElse(null);
        this.createdAt = other.optionalCreatedAt().map(InstantFilter::copy).orElse(null);
        this.competitorId = other.optionalCompetitorId().map(LongFilter::copy).orElse(null);
        this.ownerId = other.optionalOwnerId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public DataSourceCriteria copy() {
        return new DataSourceCriteria(this);
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

    public StringFilter getSourceName() {
        return sourceName;
    }

    public Optional<StringFilter> optionalSourceName() {
        return Optional.ofNullable(sourceName);
    }

    public StringFilter sourceName() {
        if (sourceName == null) {
            setSourceName(new StringFilter());
        }
        return sourceName;
    }

    public void setSourceName(StringFilter sourceName) {
        this.sourceName = sourceName;
    }

    public StringFilter getUrl() {
        return url;
    }

    public Optional<StringFilter> optionalUrl() {
        return Optional.ofNullable(url);
    }

    public StringFilter url() {
        if (url == null) {
            setUrl(new StringFilter());
        }
        return url;
    }

    public void setUrl(StringFilter url) {
        this.url = url;
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

    public BooleanFilter getIsActive() {
        return isActive;
    }

    public Optional<BooleanFilter> optionalIsActive() {
        return Optional.ofNullable(isActive);
    }

    public BooleanFilter isActive() {
        if (isActive == null) {
            setIsActive(new BooleanFilter());
        }
        return isActive;
    }

    public void setIsActive(BooleanFilter isActive) {
        this.isActive = isActive;
    }

    public InstantFilter getLastCheckedAt() {
        return lastCheckedAt;
    }

    public Optional<InstantFilter> optionalLastCheckedAt() {
        return Optional.ofNullable(lastCheckedAt);
    }

    public InstantFilter lastCheckedAt() {
        if (lastCheckedAt == null) {
            setLastCheckedAt(new InstantFilter());
        }
        return lastCheckedAt;
    }

    public void setLastCheckedAt(InstantFilter lastCheckedAt) {
        this.lastCheckedAt = lastCheckedAt;
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

    public LongFilter getCompetitorId() {
        return competitorId;
    }

    public Optional<LongFilter> optionalCompetitorId() {
        return Optional.ofNullable(competitorId);
    }

    public LongFilter competitorId() {
        if (competitorId == null) {
            setCompetitorId(new LongFilter());
        }
        return competitorId;
    }

    public void setCompetitorId(LongFilter competitorId) {
        this.competitorId = competitorId;
    }

    public LongFilter getOwnerId() {
        return ownerId;
    }

    public Optional<LongFilter> optionalOwnerId() {
        return Optional.ofNullable(ownerId);
    }

    public LongFilter ownerId() {
        if (ownerId == null) {
            setOwnerId(new LongFilter());
        }
        return ownerId;
    }

    public void setOwnerId(LongFilter ownerId) {
        this.ownerId = ownerId;
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
        final DataSourceCriteria that = (DataSourceCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(sourceName, that.sourceName) &&
            Objects.equals(url, that.url) &&
            Objects.equals(sourceType, that.sourceType) &&
            Objects.equals(isActive, that.isActive) &&
            Objects.equals(lastCheckedAt, that.lastCheckedAt) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(competitorId, that.competitorId) &&
            Objects.equals(ownerId, that.ownerId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sourceName, url, sourceType, isActive, lastCheckedAt, createdAt, competitorId, ownerId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DataSourceCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalSourceName().map(f -> "sourceName=" + f + ", ").orElse("") +
            optionalUrl().map(f -> "url=" + f + ", ").orElse("") +
            optionalSourceType().map(f -> "sourceType=" + f + ", ").orElse("") +
            optionalIsActive().map(f -> "isActive=" + f + ", ").orElse("") +
            optionalLastCheckedAt().map(f -> "lastCheckedAt=" + f + ", ").orElse("") +
            optionalCreatedAt().map(f -> "createdAt=" + f + ", ").orElse("") +
            optionalCompetitorId().map(f -> "competitorId=" + f + ", ").orElse("") +
            optionalOwnerId().map(f -> "ownerId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
