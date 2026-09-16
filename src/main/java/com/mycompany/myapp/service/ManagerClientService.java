package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.enumeration.ManagerPermissionType;
import com.mycompany.myapp.repository.ManagerAssignmentRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.dto.ManagerClientDTO;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class ManagerClientService {

    private final ManagerAssignmentRepository managerAssignmentRepository;

    public ManagerClientService(ManagerAssignmentRepository managerAssignmentRepository) {
        this.managerAssignmentRepository = managerAssignmentRepository;
    }

    public List<ManagerClientDTO> getCurrentManagerClients() {
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ROLE_MANAGER is required");
        }

        Long managerId = SecurityUtils.getCurrentUserId().orElseThrow(() ->
            new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current user id not found")
        );

        return managerAssignmentRepository
            .findAllActiveByManagerIdWithRelationships(managerId)
            .stream()
            .filter(assignment ->
                assignment
                    .getPermissions()
                    .stream()
                    .anyMatch(permission -> permission.getPermission() == ManagerPermissionType.VIEW)
            )
            .map(ManagerClientDTO::new)
            .toList();
    }
}
