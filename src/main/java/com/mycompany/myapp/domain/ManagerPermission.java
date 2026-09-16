package com.mycompany.myapp.domain;

import com.mycompany.myapp.domain.enumeration.ManagerPermissionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(
    name = "manager_permission",
    uniqueConstraints = {
        @UniqueConstraint(name = "ux_manager_permission_assignment_type", columnNames = { "assignment_id", "permission" }),
    }
)
public class ManagerPermission implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private ManagerAssignment assignment;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false, length = 50)
    private ManagerPermissionType permission;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ManagerAssignment getAssignment() {
        return assignment;
    }

    public void setAssignment(ManagerAssignment assignment) {
        this.assignment = assignment;
    }

    public ManagerPermissionType getPermission() {
        return permission;
    }

    public void setPermission(ManagerPermissionType permission) {
        this.permission = permission;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ManagerPermission)) {
            return false;
        }

        return id != null && id.equals(((ManagerPermission) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
