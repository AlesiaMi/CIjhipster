package com.mycompany.myapp.service.dto;

import com.mycompany.myapp.domain.ManagerAssignment;
import com.mycompany.myapp.domain.ManagerPermission;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.ManagerPermissionType;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ManagerClientDTO {

    private Long userId;
    private String login;
    private String firstName;
    private String lastName;
    private String email;

    private Set<ManagerPermissionType> permissions = new LinkedHashSet<>();

    public ManagerClientDTO() {}

    public ManagerClientDTO(ManagerAssignment assignment) {
        User client = assignment.getClientUser();

        this.userId = client.getId();
        this.login = client.getLogin();
        this.firstName = client.getFirstName();
        this.lastName = client.getLastName();
        this.email = client.getEmail();

        this.permissions = assignment
            .getPermissions()
            .stream()
            .map(ManagerPermission::getPermission)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<ManagerPermissionType> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<ManagerPermissionType> permissions) {
        this.permissions = permissions;
    }
}
