package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * A AnalystProfile.
 */
@Entity
@Table(name = "analyst_profile")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AnalystProfile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 255)
    @Column(name = "display_name", length = 255, nullable = false)
    private String displayName;

    @Size(max = 100)
    @Column(name = "telegram_chat_id", length = 100)
    private String telegramChatId;

    @NotNull
    @Column(name = "notification_enabled", nullable = false)
    private Boolean notificationEnabled;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    @JoinColumn(unique = true)
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "rel_analyst_profile__competitors",
        joinColumns = @JoinColumn(name = "analyst_profile_id"),
        inverseJoinColumns = @JoinColumn(name = "competitors_id")
    )
    @JsonIgnoreProperties(value = { "analystProfileses" }, allowSetters = true)
    private Set<Competitor> competitorses = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AnalystProfile id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public AnalystProfile displayName(String displayName) {
        this.setDisplayName(displayName);
        return this;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getTelegramChatId() {
        return this.telegramChatId;
    }

    public AnalystProfile telegramChatId(String telegramChatId) {
        this.setTelegramChatId(telegramChatId);
        return this;
    }

    public void setTelegramChatId(String telegramChatId) {
        this.telegramChatId = telegramChatId;
    }

    public Boolean getNotificationEnabled() {
        return this.notificationEnabled;
    }

    public AnalystProfile notificationEnabled(Boolean notificationEnabled) {
        this.setNotificationEnabled(notificationEnabled);
        return this;
    }

    public void setNotificationEnabled(Boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public AnalystProfile createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public AnalystProfile user(User user) {
        this.setUser(user);
        return this;
    }

    public Set<Competitor> getCompetitorses() {
        return this.competitorses;
    }

    public void setCompetitorses(Set<Competitor> competitors) {
        this.competitorses = competitors;
    }

    public AnalystProfile competitorses(Set<Competitor> competitors) {
        this.setCompetitorses(competitors);
        return this;
    }

    public AnalystProfile addCompetitors(Competitor competitor) {
        this.competitorses.add(competitor);
        return this;
    }

    public AnalystProfile removeCompetitors(Competitor competitor) {
        this.competitorses.remove(competitor);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AnalystProfile)) {
            return false;
        }
        return getId() != null && getId().equals(((AnalystProfile) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AnalystProfile{" +
            "id=" + getId() +
            ", displayName='" + getDisplayName() + "'" +
            ", telegramChatId='" + getTelegramChatId() + "'" +
            ", notificationEnabled='" + getNotificationEnabled() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
