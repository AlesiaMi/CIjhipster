package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * A NewsItem.
 */
@Entity
@Table(name = "news_item")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class NewsItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Size(max = 255)
    @Column(name = "external_id", length = 255)
    private String externalId;

    @NotNull
    @Size(max = 500)
    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @NotNull
    @Size(max = 1000)
    @Column(name = "url", length = 1000, nullable = false)
    private String url;

    @Size(max = 10000)
    @Column(name = "original_text", length = 10000)
    private String originalText;

    @Column(name = "published_at")
    private Instant publishedAt;

    @NotNull
    @Column(name = "collected_at", nullable = false)
    private Instant collectedAt;

    @NotNull
    @Column(name = "is_duplicate", nullable = false)
    private Boolean isDuplicate;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "competitor" }, allowSetters = true)
    private DataSource dataSource;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "analystProfileses" }, allowSetters = true)
    private Competitor competitor;

    @ManyToOne(fetch = FetchType.LAZY)
    private CollectionRun collectionRun;

    @JsonIgnoreProperties(value = { "newsItem" }, allowSetters = true)
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "newsItem")
    private AnalysisResult analysisResult;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public NewsItem id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public NewsItem externalId(String externalId) {
        this.setExternalId(externalId);
        return this;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getTitle() {
        return this.title;
    }

    public NewsItem title(String title) {
        this.setTitle(title);
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return this.url;
    }

    public NewsItem url(String url) {
        this.setUrl(url);
        return this;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOriginalText() {
        return this.originalText;
    }

    public NewsItem originalText(String originalText) {
        this.setOriginalText(originalText);
        return this;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public Instant getPublishedAt() {
        return this.publishedAt;
    }

    public NewsItem publishedAt(Instant publishedAt) {
        this.setPublishedAt(publishedAt);
        return this;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Instant getCollectedAt() {
        return this.collectedAt;
    }

    public NewsItem collectedAt(Instant collectedAt) {
        this.setCollectedAt(collectedAt);
        return this;
    }

    public void setCollectedAt(Instant collectedAt) {
        this.collectedAt = collectedAt;
    }

    public Boolean getIsDuplicate() {
        return this.isDuplicate;
    }

    public NewsItem isDuplicate(Boolean isDuplicate) {
        this.setIsDuplicate(isDuplicate);
        return this;
    }

    public void setIsDuplicate(Boolean isDuplicate) {
        this.isDuplicate = isDuplicate;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public NewsItem dataSource(DataSource dataSource) {
        this.setDataSource(dataSource);
        return this;
    }

    public Competitor getCompetitor() {
        return this.competitor;
    }

    public void setCompetitor(Competitor competitor) {
        this.competitor = competitor;
    }

    public NewsItem competitor(Competitor competitor) {
        this.setCompetitor(competitor);
        return this;
    }

    public CollectionRun getCollectionRun() {
        return this.collectionRun;
    }

    public void setCollectionRun(CollectionRun collectionRun) {
        this.collectionRun = collectionRun;
    }

    public NewsItem collectionRun(CollectionRun collectionRun) {
        this.setCollectionRun(collectionRun);
        return this;
    }

    public AnalysisResult getAnalysisResult() {
        return this.analysisResult;
    }

    public void setAnalysisResult(AnalysisResult analysisResult) {
        if (this.analysisResult != null) {
            this.analysisResult.setNewsItem(null);
        }
        if (analysisResult != null) {
            analysisResult.setNewsItem(this);
        }
        this.analysisResult = analysisResult;
    }

    public NewsItem analysisResult(AnalysisResult analysisResult) {
        this.setAnalysisResult(analysisResult);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NewsItem)) {
            return false;
        }
        return getId() != null && getId().equals(((NewsItem) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "NewsItem{" +
            "id=" + getId() +
            ", externalId='" + getExternalId() + "'" +
            ", title='" + getTitle() + "'" +
            ", url='" + getUrl() + "'" +
            ", originalText='" + getOriginalText() + "'" +
            ", publishedAt='" + getPublishedAt() + "'" +
            ", collectedAt='" + getCollectedAt() + "'" +
            ", isDuplicate='" + getIsDuplicate() + "'" +
            "}";
    }
}
