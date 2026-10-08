package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycompany.myapp.domain.enumeration.AnalysisStatus;
import com.mycompany.myapp.domain.enumeration.Sentiment;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * A AnalysisResult.
 */
@Entity
@Table(name = "analysis_result")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AnalysisResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Size(max = 4000)
    @Column(name = "summary", length = 4000)
    private String summary;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment", nullable = false)
    private Sentiment sentiment;

    @Size(max = 255)
    @Column(name = "topic", length = 255)
    private String topic;

    @Size(max = 4000)
    @Column(name = "entities", length = 4000)
    private String entities;

    @Size(max = 255)
    @Column(name = "risk_source", length = 255)
    private String riskSource;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AnalysisStatus status;

    @Size(max = 100)
    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "analyzed_at")
    private Instant analyzedAt;

    @Size(max = 4000)
    @Column(name = "error_message", length = 4000)
    private String errorMessage;

    @JsonIgnoreProperties(value = { "dataSource", "competitor", "collectionRun", "analysisResult" }, allowSetters = true)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    @JoinColumn(unique = true)
    private NewsItem newsItem;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AnalysisResult id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSummary() {
        return this.summary;
    }

    public AnalysisResult summary(String summary) {
        this.setSummary(summary);
        return this;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Sentiment getSentiment() {
        return this.sentiment;
    }

    public AnalysisResult sentiment(Sentiment sentiment) {
        this.setSentiment(sentiment);
        return this;
    }

    public void setSentiment(Sentiment sentiment) {
        this.sentiment = sentiment;
    }

    public String getTopic() {
        return this.topic;
    }

    public AnalysisResult topic(String topic) {
        this.setTopic(topic);
        return this;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getEntities() {
        return this.entities;
    }

    public AnalysisResult entities(String entities) {
        this.setEntities(entities);
        return this;
    }

    public void setEntities(String entities) {
        this.entities = entities;
    }

    public String getRiskSource() {
        return this.riskSource;
    }

    public AnalysisResult riskSource(String riskSource) {
        this.setRiskSource(riskSource);
        return this;
    }

    public void setRiskSource(String riskSource) {
        this.riskSource = riskSource;
    }

    public AnalysisStatus getStatus() {
        return this.status;
    }

    public AnalysisResult status(AnalysisStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(AnalysisStatus status) {
        this.status = status;
    }

    public String getModelName() {
        return this.modelName;
    }

    public AnalysisResult modelName(String modelName) {
        this.setModelName(modelName);
        return this;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Instant getAnalyzedAt() {
        return this.analyzedAt;
    }

    public AnalysisResult analyzedAt(Instant analyzedAt) {
        this.setAnalyzedAt(analyzedAt);
        return this;
    }

    public void setAnalyzedAt(Instant analyzedAt) {
        this.analyzedAt = analyzedAt;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public AnalysisResult errorMessage(String errorMessage) {
        this.setErrorMessage(errorMessage);
        return this;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public NewsItem getNewsItem() {
        return this.newsItem;
    }

    public void setNewsItem(NewsItem newsItem) {
        this.newsItem = newsItem;
    }

    public AnalysisResult newsItem(NewsItem newsItem) {
        this.setNewsItem(newsItem);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AnalysisResult)) {
            return false;
        }
        return getId() != null && getId().equals(((AnalysisResult) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AnalysisResult{" +
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
            "}";
    }
}
