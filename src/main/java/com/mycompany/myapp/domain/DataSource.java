package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycompany.myapp.domain.enumeration.SourceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * A DataSource.
 */
@Entity
@Table(name = "data_source")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DataSource implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 255)
    @Column(name = "source_name", length = 255, nullable = false)
    private String sourceName;

    @NotNull
    @Size(max = 1000)
    @Column(name = "url", length = 1000, nullable = false)
    private String url;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "last_checked_at")
    private Instant lastCheckedAt;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "analystProfileses" }, allowSetters = true)
    private Competitor competitor;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public DataSource id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceName() {
        return this.sourceName;
    }

    public DataSource sourceName(String sourceName) {
        this.setSourceName(sourceName);
        return this;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getUrl() {
        return this.url;
    }

    public DataSource url(String url) {
        this.setUrl(url);
        return this;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public SourceType getSourceType() {
        return this.sourceType;
    }

    public DataSource sourceType(SourceType sourceType) {
        this.setSourceType(sourceType);
        return this;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public Boolean getIsActive() {
        return this.isActive;
    }

    public DataSource isActive(Boolean isActive) {
        this.setIsActive(isActive);
        return this;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Instant getLastCheckedAt() {
        return this.lastCheckedAt;
    }

    public DataSource lastCheckedAt(Instant lastCheckedAt) {
        this.setLastCheckedAt(lastCheckedAt);
        return this;
    }

    public void setLastCheckedAt(Instant lastCheckedAt) {
        this.lastCheckedAt = lastCheckedAt;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public DataSource createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Competitor getCompetitor() {
        return this.competitor;
    }

    public void setCompetitor(Competitor competitor) {
        this.competitor = competitor;
    }

    public DataSource competitor(Competitor competitor) {
        this.setCompetitor(competitor);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DataSource)) {
            return false;
        }
        return getId() != null && getId().equals(((DataSource) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DataSource{" +
            "id=" + getId() +
            ", sourceName='" + getSourceName() + "'" +
            ", url='" + getUrl() + "'" +
            ", sourceType='" + getSourceType() + "'" +
            ", isActive='" + getIsActive() + "'" +
            ", lastCheckedAt='" + getLastCheckedAt() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
