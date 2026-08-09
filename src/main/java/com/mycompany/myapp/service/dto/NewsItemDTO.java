package com.mycompany.myapp.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.NewsItem} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class NewsItemDTO implements Serializable {

    private Long id;

    @Size(max = 255)
    private String externalId;

    @NotNull
    @Size(max = 500)
    private String title;

    @NotNull
    @Size(max = 1000)
    private String url;

    @Size(max = 10000)
    private String originalText;

    private Instant publishedAt;

    @NotNull
    private Instant collectedAt;

    @NotNull
    private Boolean isDuplicate;

    @NotNull
    private DataSourceDTO dataSource;

    @NotNull
    private CompetitorDTO competitor;

    private CollectionRunDTO collectionRun;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Instant getCollectedAt() {
        return collectedAt;
    }

    public void setCollectedAt(Instant collectedAt) {
        this.collectedAt = collectedAt;
    }

    public Boolean getIsDuplicate() {
        return isDuplicate;
    }

    public void setIsDuplicate(Boolean isDuplicate) {
        this.isDuplicate = isDuplicate;
    }

    public DataSourceDTO getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSourceDTO dataSource) {
        this.dataSource = dataSource;
    }

    public CompetitorDTO getCompetitor() {
        return competitor;
    }

    public void setCompetitor(CompetitorDTO competitor) {
        this.competitor = competitor;
    }

    public CollectionRunDTO getCollectionRun() {
        return collectionRun;
    }

    public void setCollectionRun(CollectionRunDTO collectionRun) {
        this.collectionRun = collectionRun;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NewsItemDTO)) {
            return false;
        }

        NewsItemDTO newsItemDTO = (NewsItemDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, newsItemDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "NewsItemDTO{" +
            "id=" + getId() +
            ", externalId='" + getExternalId() + "'" +
            ", title='" + getTitle() + "'" +
            ", url='" + getUrl() + "'" +
            ", originalText='" + getOriginalText() + "'" +
            ", publishedAt='" + getPublishedAt() + "'" +
            ", collectedAt='" + getCollectedAt() + "'" +
            ", isDuplicate='" + getIsDuplicate() + "'" +
            ", dataSource=" + getDataSource() +
            ", competitor=" + getCompetitor() +
            ", collectionRun=" + getCollectionRun() +
            "}";
    }
}
