package com.mycompany.myapp.service.dto;

import com.mycompany.myapp.domain.ManagerAssignment;
import com.mycompany.myapp.domain.ManagerPermission;
import com.mycompany.myapp.domain.enumeration.ManagerPermissionType;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ManagerAssignmentDTO {

    private Long id;

    private Long managerId;
    private String managerLogin;

    private Long clientUserId;
    private String clientLogin;

    private Boolean active;

    private Instant createdAt;

    private Set<ManagerPermissionType> permissions = new LinkedHashSet<>();

    public ManagerAssignmentDTO() {}

    public ManagerAssignmentDTO(ManagerAssignment assignment) {
        this.id = assignment.getId();

        if (assignment.getManager() != null) {
            this.managerId = assignment.getManager().getId();

            this.managerLogin = assignment.getManager().getLogin();
        }

        if (assignment.getClientUser() != null) {
            this.clientUserId = assignment.getClientUser().getId();

            this.clientLogin = assignment.getClientUser().getLogin();
        }

        this.active = assignment.getActive();
        this.createdAt = assignment.getCreatedAt();

        if (assignment.getPermissions() != null) {
            this.permissions = assignment
                .getPermissions()
                .stream()
                .map(ManagerPermission::getPermission)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public String getManagerLogin() {
        return managerLogin;
    }

    public void setManagerLogin(String managerLogin) {
        this.managerLogin = managerLogin;
    }

    public Long getClientUserId() {
        return clientUserId;
    }

    public void setClientUserId(Long clientUserId) {
        this.clientUserId = clientUserId;
    }

    public String getClientLogin() {
        return clientLogin;
    }

    public void setClientLogin(String clientLogin) {
        this.clientLogin = clientLogin;
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

    public Set<ManagerPermissionType> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<ManagerPermissionType> permissions) {
        this.permissions = permissions;
    }
}
