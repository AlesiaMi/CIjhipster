package com.mycompany.myapp.service.dto;

import com.mycompany.myapp.domain.enumeration.ManagerPermissionType;
import jakarta.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import java.util.Set;

public class ManagerAssignmentRequest {

    @NotNull
    private Long managerId;

    @NotNull
    private Long clientUserId;

    private Boolean active = true;

    private Set<ManagerPermissionType> permissions = new LinkedHashSet<>();

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public Long getClientUserId() {
        return clientUserId;
    }

    public void setClientUserId(Long clientUserId) {
        this.clientUserId = clientUserId;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<ManagerPermissionType> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<ManagerPermissionType> permissions) {
        this.permissions = permissions;
    }
}
