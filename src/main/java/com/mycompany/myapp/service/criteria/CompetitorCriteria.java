package com.mycompany.myapp.service.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.mycompany.myapp.domain.Competitor} entity. This class is used
 * in {@link com.mycompany.myapp.web.rest.CompetitorResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /competitors?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CompetitorCriteria implements Serializable, Criteria {

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter competitorName;

    private StringFilter websiteUrl;

    private StringFilter industry;

    private StringFilter description;

    private BooleanFilter isActive;

    private LongFilter analystProfilesId;

    private Boolean distinct;

    public CompetitorCriteria() {}

    public CompetitorCriteria(CompetitorCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.competitorName = other.optionalCompetitorName().map(StringFilter::copy).orElse(null);
        this.websiteUrl = other.optionalWebsiteUrl().map(StringFilter::copy).orElse(null);
        this.industry = other.optionalIndustry().map(StringFilter::copy).orElse(null);
        this.description = other.optionalDescription().map(StringFilter::copy).orElse(null);
        this.isActive = other.optionalIsActive().map(BooleanFilter::copy).orElse(null);
        this.analystProfilesId = other.optionalAnalystProfilesId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public CompetitorCriteria copy() {
        return new CompetitorCriteria(this);
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

    public StringFilter getCompetitorName() {
        return competitorName;
    }

    public Optional<StringFilter> optionalCompetitorName() {
        return Optional.ofNullable(competitorName);
    }

    public StringFilter competitorName() {
        if (competitorName == null) {
            setCompetitorName(new StringFilter());
        }
        return competitorName;
    }

    public void setCompetitorName(StringFilter competitorName) {
        this.competitorName = competitorName;
    }

    public StringFilter getWebsiteUrl() {
        return websiteUrl;
    }

    public Optional<StringFilter> optionalWebsiteUrl() {
        return Optional.ofNullable(websiteUrl);
    }

    public StringFilter websiteUrl() {
        if (websiteUrl == null) {
            setWebsiteUrl(new StringFilter());
        }
        return websiteUrl;
    }

    public void setWebsiteUrl(StringFilter websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public StringFilter getIndustry() {
        return industry;
    }

    public Optional<StringFilter> optionalIndustry() {
        return Optional.ofNullable(industry);
    }

    public StringFilter industry() {
        if (industry == null) {
            setIndustry(new StringFilter());
        }
        return industry;
    }

    public void setIndustry(StringFilter industry) {
        this.industry = industry;
    }

    public StringFilter getDescription() {
        return description;
    }

    public Optional<StringFilter> optionalDescription() {
        return Optional.ofNullable(description);
    }

    public StringFilter description() {
        if (description == null) {
            setDescription(new StringFilter());
        }
        return description;
    }

    public void setDescription(StringFilter description) {
        this.description = description;
    }

    public BooleanFilter getIsActive() {
        return isActive;
    }

    public Optional<BooleanFilter> optionalIsActive() {
        return Optional.ofNullable(isActive);
    }

    public BooleanFilter isActive() {
        if (isActive == null) {
            setIsActive(new BooleanFilter());
        }
        return isActive;
    }

    public void setIsActive(BooleanFilter isActive) {
        this.isActive = isActive;
    }

    public LongFilter getAnalystProfilesId() {
        return analystProfilesId;
    }

    public Optional<LongFilter> optionalAnalystProfilesId() {
        return Optional.ofNullable(analystProfilesId);
    }

    public LongFilter analystProfilesId() {
        if (analystProfilesId == null) {
            setAnalystProfilesId(new LongFilter());
        }
        return analystProfilesId;
    }

    public void setAnalystProfilesId(LongFilter analystProfilesId) {
        this.analystProfilesId = analystProfilesId;
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
        final CompetitorCriteria that = (CompetitorCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(competitorName, that.competitorName) &&
            Objects.equals(websiteUrl, that.websiteUrl) &&
            Objects.equals(industry, that.industry) &&
            Objects.equals(description, that.description) &&
            Objects.equals(isActive, that.isActive) &&
            Objects.equals(analystProfilesId, that.analystProfilesId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, competitorName, websiteUrl, industry, description, isActive, analystProfilesId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CompetitorCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalCompetitorName().map(f -> "competitorName=" + f + ", ").orElse("") +
            optionalWebsiteUrl().map(f -> "websiteUrl=" + f + ", ").orElse("") +
            optionalIndustry().map(f -> "industry=" + f + ", ").orElse("") +
            optionalDescription().map(f -> "description=" + f + ", ").orElse("") +
            optionalIsActive().map(f -> "isActive=" + f + ", ").orElse("") +
            optionalAnalystProfilesId().map(f -> "analystProfilesId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
