package com.mycompany.myapp.service.dto;

import com.mycompany.myapp.domain.enumeration.SourceType;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.DataSource} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DataSourceDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 255)
    private String sourceName;

    @NotNull
    @Size(max = 1000)
    private String url;

    @NotNull
    private SourceType sourceType;

    @NotNull
    private Boolean isActive;

    private Instant lastCheckedAt;

    @NotNull
    private Instant createdAt;

    @NotNull
    private CompetitorDTO competitor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Instant getLastCheckedAt() {
        return lastCheckedAt;
    }

    public void setLastCheckedAt(Instant lastCheckedAt) {
        this.lastCheckedAt = lastCheckedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public CompetitorDTO getCompetitor() {
        return competitor;
    }

    public void setCompetitor(CompetitorDTO competitor) {
        this.competitor = competitor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DataSourceDTO)) {
            return false;
        }

        DataSourceDTO dataSourceDTO = (DataSourceDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, dataSourceDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DataSourceDTO{" +
            "id=" + getId() +
            ", sourceName='" + getSourceName() + "'" +
            ", url='" + getUrl() + "'" +
            ", sourceType='" + getSourceType() + "'" +
            ", isActive='" + getIsActive() + "'" +
            ", lastCheckedAt='" + getLastCheckedAt() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", competitor=" + getCompetitor() +
            "}";
    }
}
