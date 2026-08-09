package com.mycompany.myapp.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.AnalystProfile} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AnalystProfileDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 255)
    private String displayName;

    @Size(max = 100)
    private String telegramChatId;

    @NotNull
    private Boolean notificationEnabled;

    @NotNull
    private Instant createdAt;

    @NotNull
    private UserDTO user;

    private Set<CompetitorDTO> competitorses = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getTelegramChatId() {
        return telegramChatId;
    }

    public void setTelegramChatId(String telegramChatId) {
        this.telegramChatId = telegramChatId;
    }

    public Boolean getNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationEnabled(Boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public Set<CompetitorDTO> getCompetitorses() {
        return competitorses;
    }

    public void setCompetitorses(Set<CompetitorDTO> competitorses) {
        this.competitorses = competitorses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AnalystProfileDTO)) {
            return false;
        }

        AnalystProfileDTO analystProfileDTO = (AnalystProfileDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, analystProfileDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AnalystProfileDTO{" +
            "id=" + getId() +
            ", displayName='" + getDisplayName() + "'" +
            ", telegramChatId='" + getTelegramChatId() + "'" +
            ", notificationEnabled='" + getNotificationEnabled() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", user=" + getUser() +
            ", competitorses=" + getCompetitorses() +
            "}";
    }
}
