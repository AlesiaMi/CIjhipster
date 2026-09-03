package com.mycompany.myapp.service.dto;

import com.mycompany.myapp.domain.enumeration.AlertStatus;
import com.mycompany.myapp.domain.enumeration.Severity;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.CiAlert} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CiAlertDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 255)
    private String title;

    @NotNull
    @Size(max = 4000)
    private String message;

    @NotNull
    private Severity severity;

    @NotNull
    private AlertStatus status;

    @NotNull
    private Instant createdAt;

    private Instant readAt;

    @NotNull
    private AnalysisResultDTO analysisResult;

    @NotNull
    private AnalystProfileDTO analystProfile;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void setReadAt(Instant readAt) {
        this.readAt = readAt;
    }

    public AnalysisResultDTO getAnalysisResult() {
        return analysisResult;
    }

    public void setAnalysisResult(AnalysisResultDTO analysisResult) {
        this.analysisResult = analysisResult;
    }

    public AnalystProfileDTO getAnalystProfile() {
        return analystProfile;
    }

    public void setAnalystProfile(AnalystProfileDTO analystProfile) {
        this.analystProfile = analystProfile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CiAlertDTO)) {
            return false;
        }

        CiAlertDTO ciAlertDTO = (CiAlertDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, ciAlertDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return (
            "CiAlertDTO{" +
            "id=" +
            getId() +
            ", title='" +
            getTitle() +
            "'" +
            ", message='" +
            getMessage() +
            "'" +
            ", severity='" +
            getSeverity() +
            "'" +
            ", status='" +
            getStatus() +
            "'" +
            ", createdAt='" +
            getCreatedAt() +
            "'" +
            ", readAt='" +
            getReadAt() +
            "'" +
            ", analysisResult=" +
            getAnalysisResult() +
            ", analystProfile=" +
            getAnalystProfile() +
            "}"
        );
    }
}
