package com.mycompany.myapp.service.criteria;

import com.mycompany.myapp.domain.enumeration.AlertStatus;
import com.mycompany.myapp.domain.enumeration.Severity;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.mycompany.myapp.domain.CiAlert} entity. This class is used
 * in {@link com.mycompany.myapp.web.rest.CiAlertResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /ci-alerts?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CiAlertCriteria implements Serializable, Criteria {

    /**
     * Class for filtering Severity
     */
    public static class SeverityFilter extends Filter<Severity> {

        public SeverityFilter() {}

        public SeverityFilter(SeverityFilter filter) {
            super(filter);
        }

        @Override
        public SeverityFilter copy() {
            return new SeverityFilter(this);
        }
    }

    /**
     * Class for filtering AlertStatus
     */
    public static class AlertStatusFilter extends Filter<AlertStatus> {

        public AlertStatusFilter() {}

        public AlertStatusFilter(AlertStatusFilter filter) {
            super(filter);
        }

        @Override
        public AlertStatusFilter copy() {
            return new AlertStatusFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter title;

    private StringFilter message;

    private SeverityFilter severity;

    private AlertStatusFilter status;

    private InstantFilter createdAt;

    private InstantFilter readAt;

    private LongFilter analysisResultId;

    private LongFilter analystProfileId;

    private Boolean distinct;

    public CiAlertCriteria() {}

    public CiAlertCriteria(CiAlertCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.title = other.optionalTitle().map(StringFilter::copy).orElse(null);
        this.message = other.optionalMessage().map(StringFilter::copy).orElse(null);
        this.severity = other.optionalSeverity().map(SeverityFilter::copy).orElse(null);
        this.status = other.optionalStatus().map(AlertStatusFilter::copy).orElse(null);
        this.createdAt = other.optionalCreatedAt().map(InstantFilter::copy).orElse(null);
        this.readAt = other.optionalReadAt().map(InstantFilter::copy).orElse(null);
        this.analysisResultId = other.optionalAnalysisResultId().map(LongFilter::copy).orElse(null);
        this.analystProfileId = other.optionalAnalystProfileId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public CiAlertCriteria copy() {
        return new CiAlertCriteria(this);
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

    public StringFilter getMessage() {
        return message;
    }

    public Optional<StringFilter> optionalMessage() {
        return Optional.ofNullable(message);
    }

    public StringFilter message() {
        if (message == null) {
            setMessage(new StringFilter());
        }
        return message;
    }

    public void setMessage(StringFilter message) {
        this.message = message;
    }

    public SeverityFilter getSeverity() {
        return severity;
    }

    public Optional<SeverityFilter> optionalSeverity() {
        return Optional.ofNullable(severity);
    }

    public SeverityFilter severity() {
        if (severity == null) {
            setSeverity(new SeverityFilter());
        }
        return severity;
    }

    public void setSeverity(SeverityFilter severity) {
        this.severity = severity;
    }

    public AlertStatusFilter getStatus() {
        return status;
    }

    public Optional<AlertStatusFilter> optionalStatus() {
        return Optional.ofNullable(status);
    }

    public AlertStatusFilter status() {
        if (status == null) {
            setStatus(new AlertStatusFilter());
        }
        return status;
    }

    public void setStatus(AlertStatusFilter status) {
        this.status = status;
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

    public InstantFilter getReadAt() {
        return readAt;
    }

    public Optional<InstantFilter> optionalReadAt() {
        return Optional.ofNullable(readAt);
    }

    public InstantFilter readAt() {
        if (readAt == null) {
            setReadAt(new InstantFilter());
        }
        return readAt;
    }

    public void setReadAt(InstantFilter readAt) {
        this.readAt = readAt;
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

    public LongFilter getAnalystProfileId() {
        return analystProfileId;
    }

    public Optional<LongFilter> optionalAnalystProfileId() {
        return Optional.ofNullable(analystProfileId);
    }

    public LongFilter analystProfileId() {
        if (analystProfileId == null) {
            setAnalystProfileId(new LongFilter());
        }
        return analystProfileId;
    }

    public void setAnalystProfileId(LongFilter analystProfileId) {
        this.analystProfileId = analystProfileId;
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
        final CiAlertCriteria that = (CiAlertCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(title, that.title) &&
            Objects.equals(message, that.message) &&
            Objects.equals(severity, that.severity) &&
            Objects.equals(status, that.status) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(readAt, that.readAt) &&
            Objects.equals(analysisResultId, that.analysisResultId) &&
            Objects.equals(analystProfileId, that.analystProfileId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, message, severity, status, createdAt, readAt, analysisResultId, analystProfileId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CiAlertCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalTitle().map(f -> "title=" + f + ", ").orElse("") +
            optionalMessage().map(f -> "message=" + f + ", ").orElse("") +
            optionalSeverity().map(f -> "severity=" + f + ", ").orElse("") +
            optionalStatus().map(f -> "status=" + f + ", ").orElse("") +
            optionalCreatedAt().map(f -> "createdAt=" + f + ", ").orElse("") +
            optionalReadAt().map(f -> "readAt=" + f + ", ").orElse("") +
            optionalAnalysisResultId().map(f -> "analysisResultId=" + f + ", ").orElse("") +
            optionalAnalystProfileId().map(f -> "analystProfileId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
