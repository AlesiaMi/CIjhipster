package com.mycompany.myapp.domain;

import com.mycompany.myapp.domain.enumeration.RunStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * A CollectionRun.
 */
@Entity
@Table(name = "collection_run")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CollectionRun implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RunStatus status;

    @Column(name = "found_count")
    private Integer foundCount;

    @Column(name = "processed_count")
    private Integer processedCount;

    @Size(max = 4000)
    @Column(name = "error_message", length = 4000)
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public CollectionRun id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getStartedAt() {
        return this.startedAt;
    }

    public CollectionRun startedAt(Instant startedAt) {
        this.setStartedAt(startedAt);
        return this;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return this.finishedAt;
    }

    public CollectionRun finishedAt(Instant finishedAt) {
        this.setFinishedAt(finishedAt);
        return this;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public RunStatus getStatus() {
        return this.status;
    }

    public CollectionRun status(RunStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(RunStatus status) {
        this.status = status;
    }

    public Integer getFoundCount() {
        return this.foundCount;
    }

    public CollectionRun foundCount(Integer foundCount) {
        this.setFoundCount(foundCount);
        return this;
    }

    public void setFoundCount(Integer foundCount) {
        this.foundCount = foundCount;
    }

    public Integer getProcessedCount() {
        return this.processedCount;
    }

    public CollectionRun processedCount(Integer processedCount) {
        this.setProcessedCount(processedCount);
        return this;
    }

    public void setProcessedCount(Integer processedCount) {
        this.processedCount = processedCount;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public CollectionRun errorMessage(String errorMessage) {
        this.setErrorMessage(errorMessage);
        return this;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public User getOwner() {
        return this.owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public CollectionRun owner(User owner) {
        this.setOwner(owner);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CollectionRun)) {
            return false;
        }
        return getId() != null && getId().equals(((CollectionRun) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CollectionRun{" +
            "id=" + getId() +
            ", startedAt='" + getStartedAt() + "'" +
            ", finishedAt='" + getFinishedAt() + "'" +
            ", status='" + getStatus() + "'" +
            ", foundCount=" + getFoundCount() +
            ", processedCount=" + getProcessedCount() +
            ", errorMessage='" + getErrorMessage() + "'" +
            ", ownerId=" + (getOwner() != null ? getOwner().getId() : null) +
            "}";
    }
}
