package com.mycompany.myapp.service.dto;

import com.mycompany.myapp.domain.enumeration.RunStatus;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.CollectionRun} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CollectionRunDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant startedAt;

    private Instant finishedAt;

    @NotNull
    private RunStatus status;

    private Integer foundCount;

    private Integer processedCount;

    @Size(max = 4000)
    private String errorMessage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public RunStatus getStatus() {
        return status;
    }

    public void setStatus(RunStatus status) {
        this.status = status;
    }

    public Integer getFoundCount() {
        return foundCount;
    }

    public void setFoundCount(Integer foundCount) {
        this.foundCount = foundCount;
    }

    public Integer getProcessedCount() {
        return processedCount;
    }

    public void setProcessedCount(Integer processedCount) {
        this.processedCount = processedCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CollectionRunDTO)) {
            return false;
        }

        CollectionRunDTO collectionRunDTO = (CollectionRunDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, collectionRunDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CollectionRunDTO{" +
            "id=" + getId() +
            ", startedAt='" + getStartedAt() + "'" +
            ", finishedAt='" + getFinishedAt() + "'" +
            ", status='" + getStatus() + "'" +
            ", foundCount=" + getFoundCount() +
            ", processedCount=" + getProcessedCount() +
            ", errorMessage='" + getErrorMessage() + "'" +
            "}";
    }
}
