package com.mycompany.myapp.service.criteria;

import com.mycompany.myapp.domain.enumeration.AnalysisStatus;
import com.mycompany.myapp.domain.enumeration.Sentiment;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.mycompany.myapp.domain.AnalysisResult} entity. This class is used
 * in {@link com.mycompany.myapp.web.rest.AnalysisResultResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /analysis-results?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AnalysisResultCriteria implements Serializable, Criteria {

    /**
     * Class for filtering Sentiment
     */
    public static class SentimentFilter extends Filter<Sentiment> {

        public SentimentFilter() {}

        public SentimentFilter(SentimentFilter filter) {
            super(filter);
        }

        @Override
        public SentimentFilter copy() {
            return new SentimentFilter(this);
        }
    }

    /**
     * Class for filtering AnalysisStatus
     */
    public static class AnalysisStatusFilter extends Filter<AnalysisStatus> {

        public AnalysisStatusFilter() {}

        public AnalysisStatusFilter(AnalysisStatusFilter filter) {
            super(filter);
        }

        @Override
        public AnalysisStatusFilter copy() {
            return new AnalysisStatusFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter summary;

    private SentimentFilter sentiment;

    private StringFilter topic;

    private StringFilter entities;

    private StringFilter riskSource;

    private AnalysisStatusFilter status;

    private StringFilter modelName;

    private InstantFilter analyzedAt;

    private StringFilter errorMessage;

    private LongFilter newsItemId;

    private LongFilter ownerId;

    private Boolean distinct;

    public AnalysisResultCriteria() {}

    public AnalysisResultCriteria(AnalysisResultCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.summary = other.optionalSummary().map(StringFilter::copy).orElse(null);
        this.sentiment = other.optionalSentiment().map(SentimentFilter::copy).orElse(null);
        this.topic = other.optionalTopic().map(StringFilter::copy).orElse(null);
        this.entities = other.optionalEntities().map(StringFilter::copy).orElse(null);
        this.riskSource = other.optionalRiskSource().map(StringFilter::copy).orElse(null);
        this.status = other.optionalStatus().map(AnalysisStatusFilter::copy).orElse(null);
        this.modelName = other.optionalModelName().map(StringFilter::copy).orElse(null);
        this.analyzedAt = other.optionalAnalyzedAt().map(InstantFilter::copy).orElse(null);
        this.errorMessage = other.optionalErrorMessage().map(StringFilter::copy).orElse(null);
        this.newsItemId = other.optionalNewsItemId().map(LongFilter::copy).orElse(null);
        this.ownerId = other.optionalOwnerId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public AnalysisResultCriteria copy() {
        return new AnalysisResultCriteria(this);
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

    public StringFilter getSummary() {
        return summary;
    }

    public Optional<StringFilter> optionalSummary() {
        return Optional.ofNullable(summary);
    }

    public StringFilter summary() {
        if (summary == null) {
            setSummary(new StringFilter());
        }
        return summary;
    }

    public void setSummary(StringFilter summary) {
        this.summary = summary;
    }

    public SentimentFilter getSentiment() {
        return sentiment;
    }

    public Optional<SentimentFilter> optionalSentiment() {
        return Optional.ofNullable(sentiment);
    }

    public SentimentFilter sentiment() {
        if (sentiment == null) {
            setSentiment(new SentimentFilter());
        }
        return sentiment;
    }

    public void setSentiment(SentimentFilter sentiment) {
        this.sentiment = sentiment;
    }

    public StringFilter getTopic() {
        return topic;
    }

    public Optional<StringFilter> optionalTopic() {
        return Optional.ofNullable(topic);
    }

    public StringFilter topic() {
        if (topic == null) {
            setTopic(new StringFilter());
        }
        return topic;
    }

    public void setTopic(StringFilter topic) {
        this.topic = topic;
    }

    public StringFilter getEntities() {
        return entities;
    }

    public Optional<StringFilter> optionalEntities() {
        return Optional.ofNullable(entities);
    }

    public StringFilter entities() {
        if (entities == null) {
            setEntities(new StringFilter());
        }
        return entities;
    }

    public void setEntities(StringFilter entities) {
        this.entities = entities;
    }

    public StringFilter getRiskSource() {
        return riskSource;
    }

    public Optional<StringFilter> optionalRiskSource() {
        return Optional.ofNullable(riskSource);
    }

    public StringFilter riskSource() {
        if (riskSource == null) {
            setRiskSource(new StringFilter());
        }
        return riskSource;
    }

    public void setRiskSource(StringFilter riskSource) {
        this.riskSource = riskSource;
    }

    public AnalysisStatusFilter getStatus() {
        return status;
    }

    public Optional<AnalysisStatusFilter> optionalStatus() {
        return Optional.ofNullable(status);
    }

    public AnalysisStatusFilter status() {
        if (status == null) {
            setStatus(new AnalysisStatusFilter());
        }
        return status;
    }

    public void setStatus(AnalysisStatusFilter status) {
        this.status = status;
    }

    public StringFilter getModelName() {
        return modelName;
    }

    public Optional<StringFilter> optionalModelName() {
        return Optional.ofNullable(modelName);
    }

    public StringFilter modelName() {
        if (modelName == null) {
            setModelName(new StringFilter());
        }
        return modelName;
    }

    public void setModelName(StringFilter modelName) {
        this.modelName = modelName;
    }

    public InstantFilter getAnalyzedAt() {
        return analyzedAt;
    }

    public Optional<InstantFilter> optionalAnalyzedAt() {
        return Optional.ofNullable(analyzedAt);
    }

    public InstantFilter analyzedAt() {
        if (analyzedAt == null) {
            setAnalyzedAt(new InstantFilter());
        }
        return analyzedAt;
    }

    public void setAnalyzedAt(InstantFilter analyzedAt) {
        this.analyzedAt = analyzedAt;
    }

    public StringFilter getErrorMessage() {
        return errorMessage;
    }

    public Optional<StringFilter> optionalErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    public StringFilter errorMessage() {
        if (errorMessage == null) {
            setErrorMessage(new StringFilter());
        }
        return errorMessage;
    }

    public void setErrorMessage(StringFilter errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LongFilter getNewsItemId() {
        return newsItemId;
    }

    public Optional<LongFilter> optionalNewsItemId() {
        return Optional.ofNullable(newsItemId);
    }

    public LongFilter newsItemId() {
        if (newsItemId == null) {
            setNewsItemId(new LongFilter());
        }
        return newsItemId;
    }

    public void setNewsItemId(LongFilter newsItemId) {
        this.newsItemId = newsItemId;
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
        final AnalysisResultCriteria that = (AnalysisResultCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(summary, that.summary) &&
            Objects.equals(sentiment, that.sentiment) &&
            Objects.equals(topic, that.topic) &&
            Objects.equals(entities, that.entities) &&
            Objects.equals(riskSource, that.riskSource) &&
            Objects.equals(status, that.status) &&
            Objects.equals(modelName, that.modelName) &&
            Objects.equals(analyzedAt, that.analyzedAt) &&
            Objects.equals(errorMessage, that.errorMessage) &&
            Objects.equals(newsItemId, that.newsItemId) &&
            Objects.equals(ownerId, that.ownerId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            summary,
            sentiment,
            topic,
            entities,
            riskSource,
            status,
            modelName,
            analyzedAt,
            errorMessage,
            newsItemId,
            ownerId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AnalysisResultCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalSummary().map(f -> "summary=" + f + ", ").orElse("") +
            optionalSentiment().map(f -> "sentiment=" + f + ", ").orElse("") +
            optionalTopic().map(f -> "topic=" + f + ", ").orElse("") +
            optionalEntities().map(f -> "entities=" + f + ", ").orElse("") +
            optionalRiskSource().map(f -> "riskSource=" + f + ", ").orElse("") +
            optionalStatus().map(f -> "status=" + f + ", ").orElse("") +
            optionalModelName().map(f -> "modelName=" + f + ", ").orElse("") +
            optionalAnalyzedAt().map(f -> "analyzedAt=" + f + ", ").orElse("") +
            optionalErrorMessage().map(f -> "errorMessage=" + f + ", ").orElse("") +
            optionalNewsItemId().map(f -> "newsItemId=" + f + ", ").orElse("") +
            optionalOwnerId().map(f -> "ownerId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
