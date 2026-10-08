package com.mycompany.myapp.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.Keyword} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class KeywordDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 255)
    private String value;

    @NotNull
    private Boolean isActive;

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
        if (!(o instanceof KeywordDTO)) {
            return false;
        }

        KeywordDTO keywordDTO = (KeywordDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, keywordDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "KeywordDTO{" +
            "id=" + getId() +
            ", value='" + getValue() + "'" +
            ", isActive='" + getIsActive() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", competitor=" + getCompetitor() +
            "}";
    }
}
