package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A Competitor.
 */
@Entity
@Table(name = "competitor")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Competitor implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 255)
    @Column(name = "competitor_name", length = 255, nullable = false)
    private String competitorName;

    @Size(max = 500)
    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @Size(max = 255)
    @Column(name = "industry", length = 255)
    private String industry;

    @Size(max = 2000)
    @Column(name = "description", length = 2000)
    private String description;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "competitorses")
    @JsonIgnoreProperties(value = { "user", "competitorses" }, allowSetters = true)
    private Set<AnalystProfile> analystProfileses = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Competitor id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompetitorName() {
        return this.competitorName;
    }

    public Competitor competitorName(String competitorName) {
        this.setCompetitorName(competitorName);
        return this;
    }

    public void setCompetitorName(String competitorName) {
        this.competitorName = competitorName;
    }

    public String getWebsiteUrl() {
        return this.websiteUrl;
    }

    public Competitor websiteUrl(String websiteUrl) {
        this.setWebsiteUrl(websiteUrl);
        return this;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getIndustry() {
        return this.industry;
    }

    public Competitor industry(String industry) {
        this.setIndustry(industry);
        return this;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getDescription() {
        return this.description;
    }

    public Competitor description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return this.isActive;
    }

    public Competitor isActive(Boolean isActive) {
        this.setIsActive(isActive);
        return this;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Set<AnalystProfile> getAnalystProfileses() {
        return this.analystProfileses;
    }

    public void setAnalystProfileses(Set<AnalystProfile> analystProfiles) {
        if (this.analystProfileses != null) {
            this.analystProfileses.forEach(i -> i.removeCompetitors(this));
        }
        if (analystProfiles != null) {
            analystProfiles.forEach(i -> i.addCompetitors(this));
        }
        this.analystProfileses = analystProfiles;
    }

    public Competitor analystProfileses(Set<AnalystProfile> analystProfiles) {
        this.setAnalystProfileses(analystProfiles);
        return this;
    }

    public Competitor addAnalystProfiles(AnalystProfile analystProfile) {
        this.analystProfileses.add(analystProfile);
        analystProfile.getCompetitorses().add(this);
        return this;
    }

    public Competitor removeAnalystProfiles(AnalystProfile analystProfile) {
        this.analystProfileses.remove(analystProfile);
        analystProfile.getCompetitorses().remove(this);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Competitor)) {
            return false;
        }
        return getId() != null && getId().equals(((Competitor) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Competitor{" +
            "id=" + getId() +
            ", competitorName='" + getCompetitorName() + "'" +
            ", websiteUrl='" + getWebsiteUrl() + "'" +
            ", industry='" + getIndustry() + "'" +
            ", description='" + getDescription() + "'" +
            ", isActive='" + getIsActive() + "'" +
            "}";
    }
}
