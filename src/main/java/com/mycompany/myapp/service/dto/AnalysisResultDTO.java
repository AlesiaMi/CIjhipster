package com.mycompany.myapp.service.dto;

import com.mycompany.myapp.domain.enumeration.AnalysisStatus;
import com.mycompany.myapp.domain.enumeration.Sentiment;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.AnalysisResult} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AnalysisResultDTO implements Serializable {

    private Long id;

    @Size(max = 4000)
    private String summary;

    @NotNull
    private Sentiment sentiment;

    @Size(max = 255)
    private String topic;

    @Size(max = 4000)
    private String entities;

    @Size(max = 255)
    private String riskSource;

    @NotNull
    private AnalysisStatus status;

    @Size(max = 100)
    private String modelName;

    private Instant analyzedAt;

    @Size(max = 4000)
    private String errorMessage;

    @NotNull
    private NewsItemDTO newsItem;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Sentiment getSentiment() {
        return sentiment;
    }

    public void setSentiment(Sentiment sentiment) {
        this.sentiment = sentiment;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getEntities() {
        return entities;
    }

    public void setEntities(String entities) {
        this.entities = entities;
    }

    public String getRiskSource() {
        return riskSource;
    }

    public void setRiskSource(String riskSource) {
        this.riskSource = riskSource;
    }

    public AnalysisStatus getStatus() {
        return status;
    }

    public void setStatus(AnalysisStatus status) {
        this.status = status;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Instant getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(Instant analyzedAt) {
        this.analyzedAt = analyzedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public NewsItemDTO getNewsItem() {
        return newsItem;
    }

    public void setNewsItem(NewsItemDTO newsItem) {
        this.newsItem = newsItem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AnalysisResultDTO)) {
            return false;
        }

        AnalysisResultDTO analysisResultDTO = (AnalysisResultDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, analysisResultDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AnalysisResultDTO{" +
            "id=" + getId() +
            ", summary='" + getSummary() + "'" +
            ", sentiment='" + getSentiment() + "'" +
            ", topic='" + getTopic() + "'" +
            ", entities='" + getEntities() + "'" +
            ", riskSource='" + getRiskSource() + "'" +
            ", status='" + getStatus() + "'" +
            ", modelName='" + getModelName() + "'" +
            ", analyzedAt='" + getAnalyzedAt() + "'" +
            ", errorMessage='" + getErrorMessage() + "'" +
            ", newsItem=" + getNewsItem() +
            "}";
    }
}
