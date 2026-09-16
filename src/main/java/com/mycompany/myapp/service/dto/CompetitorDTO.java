package com.mycompany.myapp.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.Competitor} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CompetitorDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 255)
    private String competitorName;

    @Size(max = 500)
    private String websiteUrl;

    @Size(max = 255)
    private String industry;

    @Size(max = 2000)
    private String description;

    @NotNull
    private Boolean isActive;

    private UserDTO owner;

    private Set<AnalystProfileDTO> analystProfileses = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompetitorName() {
        return competitorName;
    }

    public void setCompetitorName(String competitorName) {
        this.competitorName = competitorName;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public UserDTO getOwner() {
        return owner;
    }

    public void setOwner(UserDTO owner) {
        this.owner = owner;
    }

    public Set<AnalystProfileDTO> getAnalystProfileses() {
        return analystProfileses;
    }

    public void setAnalystProfileses(Set<AnalystProfileDTO> analystProfileses) {
        this.analystProfileses = analystProfileses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CompetitorDTO)) {
            return false;
        }

        CompetitorDTO competitorDTO = (CompetitorDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, competitorDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CompetitorDTO{" +
            "id=" + getId() +
            ", competitorName='" + getCompetitorName() + "'" +
            ", websiteUrl='" + getWebsiteUrl() + "'" +
            ", industry='" + getIndustry() + "'" +
            ", description='" + getDescription() + "'" +
            ", isActive='" + getIsActive() + "'" +
            ", owner=" + getOwner() +
            ", analystProfileses=" + getAnalystProfileses() +
            "}";
    }
}
