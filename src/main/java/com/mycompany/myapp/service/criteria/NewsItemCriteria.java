package com.mycompany.myapp.service.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.mycompany.myapp.domain.NewsItem} entity. This class is used
 * in {@link com.mycompany.myapp.web.rest.NewsItemResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /news-items?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class NewsItemCriteria implements Serializable, Criteria {

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter externalId;

    private StringFilter title;

    private StringFilter url;

    private StringFilter originalText;

    private InstantFilter publishedAt;

    private InstantFilter collectedAt;

    private BooleanFilter isDuplicate;

    private LongFilter dataSourceId;

    private LongFilter competitorId;

    private LongFilter collectionRunId;

    private LongFilter analysisResultId;

    private LongFilter ownerId;

    private Boolean distinct;

    public NewsItemCriteria() {}

    public NewsItemCriteria(NewsItemCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.externalId = other.optionalExternalId().map(StringFilter::copy).orElse(null);
        this.title = other.optionalTitle().map(StringFilter::copy).orElse(null);
        this.url = other.optionalUrl().map(StringFilter::copy).orElse(null);
        this.originalText = other.optionalOriginalText().map(StringFilter::copy).orElse(null);
        this.publishedAt = other.optionalPublishedAt().map(InstantFilter::copy).orElse(null);
        this.collectedAt = other.optionalCollectedAt().map(InstantFilter::copy).orElse(null);
        this.isDuplicate = other.optionalIsDuplicate().map(BooleanFilter::copy).orElse(null);
        this.dataSourceId = other.optionalDataSourceId().map(LongFilter::copy).orElse(null);
        this.competitorId = other.optionalCompetitorId().map(LongFilter::copy).orElse(null);
        this.collectionRunId = other.optionalCollectionRunId().map(LongFilter::copy).orElse(null);
        this.analysisResultId = other.optionalAnalysisResultId().map(LongFilter::copy).orElse(null);
        this.ownerId = other.optionalOwnerId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public NewsItemCriteria copy() {
        return new NewsItemCriteria(this);
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

    public StringFilter getExternalId() {
        return externalId;
    }

    public Optional<StringFilter> optionalExternalId() {
        return Optional.ofNullable(externalId);
    }

    public StringFilter externalId() {
        if (externalId == null) {
            setExternalId(new StringFilter());
        }
        return externalId;
    }

    public void setExternalId(StringFilter externalId) {
        this.externalId = externalId;
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

    public StringFilter getOriginalText() {
        return originalText;
    }

    public Optional<StringFilter> optionalOriginalText() {
        return Optional.ofNullable(originalText);
    }

    public StringFilter originalText() {
        if (originalText == null) {
            setOriginalText(new StringFilter());
        }
        return originalText;
    }

    public void setOriginalText(StringFilter originalText) {
        this.originalText = originalText;
    }

    public InstantFilter getPublishedAt() {
        return publishedAt;
    }

    public Optional<InstantFilter> optionalPublishedAt() {
        return Optional.ofNullable(publishedAt);
    }

    public InstantFilter publishedAt() {
        if (publishedAt == null) {
            setPublishedAt(new InstantFilter());
        }
        return publishedAt;
    }

    public void setPublishedAt(InstantFilter publishedAt) {
        this.publishedAt = publishedAt;
    }

    public InstantFilter getCollectedAt() {
        return collectedAt;
    }

    public Optional<InstantFilter> optionalCollectedAt() {
        return Optional.ofNullable(collectedAt);
    }

    public InstantFilter collectedAt() {
        if (collectedAt == null) {
            setCollectedAt(new InstantFilter());
        }
        return collectedAt;
    }

    public void setCollectedAt(InstantFilter collectedAt) {
        this.collectedAt = collectedAt;
    }

    public BooleanFilter getIsDuplicate() {
        return isDuplicate;
    }

    public Optional<BooleanFilter> optionalIsDuplicate() {
        return Optional.ofNullable(isDuplicate);
    }

    public BooleanFilter isDuplicate() {
        if (isDuplicate == null) {
            setIsDuplicate(new BooleanFilter());
        }
        return isDuplicate;
    }

    public void setIsDuplicate(BooleanFilter isDuplicate) {
        this.isDuplicate = isDuplicate;
    }

    public LongFilter getDataSourceId() {
        return dataSourceId;
    }

    public Optional<LongFilter> optionalDataSourceId() {
        return Optional.ofNullable(dataSourceId);
    }

    public LongFilter dataSourceId() {
        if (dataSourceId == null) {
            setDataSourceId(new LongFilter());
        }
        return dataSourceId;
    }

    public void setDataSourceId(LongFilter dataSourceId) {
        this.dataSourceId = dataSourceId;
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

    public LongFilter getCollectionRunId() {
        return collectionRunId;
    }

    public Optional<LongFilter> optionalCollectionRunId() {
        return Optional.ofNullable(collectionRunId);
    }

    public LongFilter collectionRunId() {
        if (collectionRunId == null) {
            setCollectionRunId(new LongFilter());
        }
        return collectionRunId;
    }

    public void setCollectionRunId(LongFilter collectionRunId) {
        this.collectionRunId = collectionRunId;
    }

    public LongFilter getAnalysisResultId() {
        return analysisResultId;
    }

    public Optional<LongFilter> optionalAnalysisResultId() {
        return Optional.ofNullable(analysisResultId);
    }

    public LongFilter analysisResultId() {
        if (analysisResultId == null) {
            setAnalysisResultId(new LongFilter());
        }
        return analysisResultId;
    }

    public void setAnalysisResultId(LongFilter analysisResultId) {
        this.analysisResultId = analysisResultId;
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
        final NewsItemCriteria that = (NewsItemCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(externalId, that.externalId) &&
            Objects.equals(title, that.title) &&
            Objects.equals(url, that.url) &&
            Objects.equals(originalText, that.originalText) &&
            Objects.equals(publishedAt, that.publishedAt) &&
            Objects.equals(collectedAt, that.collectedAt) &&
            Objects.equals(isDuplicate, that.isDuplicate) &&
            Objects.equals(dataSourceId, that.dataSourceId) &&
            Objects.equals(competitorId, that.competitorId) &&
            Objects.equals(collectionRunId, that.collectionRunId) &&
            Objects.equals(analysisResultId, that.analysisResultId) &&
            Objects.equals(ownerId, that.ownerId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            externalId,
            title,
            url,
            originalText,
            publishedAt,
            collectedAt,
            isDuplicate,
            dataSourceId,
            competitorId,
            collectionRunId,
            analysisResultId,
            ownerId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "NewsItemCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalExternalId().map(f -> "externalId=" + f + ", ").orElse("") +
            optionalTitle().map(f -> "title=" + f + ", ").orElse("") +
            optionalUrl().map(f -> "url=" + f + ", ").orElse("") +
            optionalOriginalText().map(f -> "originalText=" + f + ", ").orElse("") +
            optionalPublishedAt().map(f -> "publishedAt=" + f + ", ").orElse("") +
            optionalCollectedAt().map(f -> "collectedAt=" + f + ", ").orElse("") +
            optionalIsDuplicate().map(f -> "isDuplicate=" + f + ", ").orElse("") +
            optionalDataSourceId().map(f -> "dataSourceId=" + f + ", ").orElse("") +
            optionalCompetitorId().map(f -> "competitorId=" + f + ", ").orElse("") +
            optionalCollectionRunId().map(f -> "collectionRunId=" + f + ", ").orElse("") +
            optionalAnalysisResultId().map(f -> "analysisResultId=" + f + ", ").orElse("") +
            optionalOwnerId().map(f -> "ownerId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
