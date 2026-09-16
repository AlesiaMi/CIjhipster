package com.mycompany.myapp.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
    name = "manager_assignment",
    uniqueConstraints = {
        @UniqueConstraint(name = "ux_manager_assignment_manager_client", columnNames = { "manager_id", "client_user_id" }),
    }
)
public class ManagerAssignment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_user_id", nullable = false)
    private User clientUser;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ManagerPermission> permissions = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }

        if (active == null) {
            active = true;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

    public User getClientUser() {
        return clientUser;
    }

    public void setClientUser(User clientUser) {
        this.clientUser = clientUser;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Set<ManagerPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<ManagerPermission> permissions) {
        this.permissions = permissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ManagerAssignment)) {
            return false;
        }

        return id != null && id.equals(((ManagerAssignment) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
