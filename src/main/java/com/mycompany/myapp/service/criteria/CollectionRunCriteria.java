package com.mycompany.myapp.service.criteria;

import com.mycompany.myapp.domain.enumeration.RunStatus;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.mycompany.myapp.domain.CollectionRun} entity. This class is used
 * in {@link com.mycompany.myapp.web.rest.CollectionRunResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /collection-runs?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CollectionRunCriteria implements Serializable, Criteria {

    /**
     * Class for filtering RunStatus
     */
    public static class RunStatusFilter extends Filter<RunStatus> {

        public RunStatusFilter() {}

        public RunStatusFilter(RunStatusFilter filter) {
            super(filter);
        }

        @Override
        public RunStatusFilter copy() {
            return new RunStatusFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private InstantFilter startedAt;

    private InstantFilter finishedAt;

    private RunStatusFilter status;

    private IntegerFilter foundCount;

    private IntegerFilter processedCount;

    private StringFilter errorMessage;

    private LongFilter ownerId;

    private Boolean distinct;

    public CollectionRunCriteria() {}

    public CollectionRunCriteria(CollectionRunCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.startedAt = other.optionalStartedAt().map(InstantFilter::copy).orElse(null);
        this.finishedAt = other.optionalFinishedAt().map(InstantFilter::copy).orElse(null);
        this.status = other.optionalStatus().map(RunStatusFilter::copy).orElse(null);
        this.foundCount = other.optionalFoundCount().map(IntegerFilter::copy).orElse(null);
        this.processedCount = other.optionalProcessedCount().map(IntegerFilter::copy).orElse(null);
        this.errorMessage = other.optionalErrorMessage().map(StringFilter::copy).orElse(null);
        this.ownerId = other.optionalOwnerId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public CollectionRunCriteria copy() {
        return new CollectionRunCriteria(this);
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

    public InstantFilter getStartedAt() {
        return startedAt;
    }

    public Optional<InstantFilter> optionalStartedAt() {
        return Optional.ofNullable(startedAt);
    }

    public InstantFilter startedAt() {
        if (startedAt == null) {
            setStartedAt(new InstantFilter());
        }
        return startedAt;
    }

    public void setStartedAt(InstantFilter startedAt) {
        this.startedAt = startedAt;
    }

    public InstantFilter getFinishedAt() {
        return finishedAt;
    }

    public Optional<InstantFilter> optionalFinishedAt() {
        return Optional.ofNullable(finishedAt);
    }

    public InstantFilter finishedAt() {
        if (finishedAt == null) {
            setFinishedAt(new InstantFilter());
        }
        return finishedAt;
    }

    public void setFinishedAt(InstantFilter finishedAt) {
        this.finishedAt = finishedAt;
    }

    public RunStatusFilter getStatus() {
        return status;
    }

    public Optional<RunStatusFilter> optionalStatus() {
        return Optional.ofNullable(status);
    }

    public RunStatusFilter status() {
        if (status == null) {
            setStatus(new RunStatusFilter());
        }
        return status;
    }

    public void setStatus(RunStatusFilter status) {
        this.status = status;
    }

    public IntegerFilter getFoundCount() {
        return foundCount;
    }

    public Optional<IntegerFilter> optionalFoundCount() {
        return Optional.ofNullable(foundCount);
    }

    public IntegerFilter foundCount() {
        if (foundCount == null) {
            setFoundCount(new IntegerFilter());
        }
        return foundCount;
    }

    public void setFoundCount(IntegerFilter foundCount) {
        this.foundCount = foundCount;
    }

    public IntegerFilter getProcessedCount() {
        return processedCount;
    }

    public Optional<IntegerFilter> optionalProcessedCount() {
        return Optional.ofNullable(processedCount);
    }

    public IntegerFilter processedCount() {
        if (processedCount == null) {
            setProcessedCount(new IntegerFilter());
        }
        return processedCount;
    }

    public void setProcessedCount(IntegerFilter processedCount) {
        this.processedCount = processedCount;
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
        final CollectionRunCriteria that = (CollectionRunCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(startedAt, that.startedAt) &&
            Objects.equals(finishedAt, that.finishedAt) &&
            Objects.equals(status, that.status) &&
            Objects.equals(foundCount, that.foundCount) &&
            Objects.equals(processedCount, that.processedCount) &&
            Objects.equals(errorMessage, that.errorMessage) &&
            Objects.equals(ownerId, that.ownerId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startedAt, finishedAt, status, foundCount, processedCount, errorMessage, ownerId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CollectionRunCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalStartedAt().map(f -> "startedAt=" + f + ", ").orElse("") +
            optionalFinishedAt().map(f -> "finishedAt=" + f + ", ").orElse("") +
            optionalStatus().map(f -> "status=" + f + ", ").orElse("") +
            optionalFoundCount().map(f -> "foundCount=" + f + ", ").orElse("") +
            optionalProcessedCount().map(f -> "processedCount=" + f + ", ").orElse("") +
            optionalErrorMessage().map(f -> "errorMessage=" + f + ", ").orElse("") +
            optionalOwnerId().map(f -> "ownerId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
