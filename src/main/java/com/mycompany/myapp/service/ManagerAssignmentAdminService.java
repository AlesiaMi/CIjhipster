package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.ManagerAssignment;
import com.mycompany.myapp.domain.ManagerPermission;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.ManagerPermissionType;
import com.mycompany.myapp.repository.ManagerAssignmentRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.service.dto.ManagerAssignmentDTO;
import com.mycompany.myapp.service.dto.ManagerAssignmentRequest;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ManagerAssignmentAdminService {

    private final ManagerAssignmentRepository managerAssignmentRepository;

    private final UserRepository userRepository;

    public ManagerAssignmentAdminService(ManagerAssignmentRepository managerAssignmentRepository, UserRepository userRepository) {
        this.managerAssignmentRepository = managerAssignmentRepository;

        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ManagerAssignmentDTO> findAll() {
        return managerAssignmentRepository.findAllWithRelationships().stream().map(ManagerAssignmentDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public ManagerAssignmentDTO findOne(Long id) {
        return managerAssignmentRepository
            .findOneWithRelationships(id)
            .map(ManagerAssignmentDTO::new)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Manager assignment not found"));
    }

    public ManagerAssignmentDTO create(ManagerAssignmentRequest request) {
        validateDifferentUsers(request);

        User manager = requireManager(request.getManagerId());

        User client = requireClient(request.getClientUserId());

        ManagerAssignment assignment = managerAssignmentRepository
            .findOneByManagerIdAndClientUserId(manager.getId(), client.getId())
            .orElseGet(ManagerAssignment::new);

        assignment.setManager(manager);
        assignment.setClientUser(client);

        assignment.setActive(request.getActive() == null ? true : request.getActive());

        replacePermissions(assignment, request.getPermissions());

        assignment = managerAssignmentRepository.save(assignment);

        return new ManagerAssignmentDTO(assignment);
    }

    public ManagerAssignmentDTO update(Long id, ManagerAssignmentRequest request) {
        validateDifferentUsers(request);

        ManagerAssignment assignment = managerAssignmentRepository
            .findOneWithRelationships(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Manager assignment not found"));

        User manager = requireManager(request.getManagerId());

        User client = requireClient(request.getClientUserId());

        managerAssignmentRepository
            .findOneByManagerIdAndClientUserId(manager.getId(), client.getId())
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This manager is already assigned to this client");
            });

        assignment.setManager(manager);
        assignment.setClientUser(client);

        assignment.setActive(request.getActive() == null ? assignment.getActive() : request.getActive());

        replacePermissions(assignment, request.getPermissions());

        assignment = managerAssignmentRepository.save(assignment);

        return new ManagerAssignmentDTO(assignment);
    }

    public void delete(Long id) {
        ManagerAssignment assignment = managerAssignmentRepository
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Manager assignment not found"));

        managerAssignmentRepository.delete(assignment);
    }

    private void replacePermissions(ManagerAssignment assignment, Set<ManagerPermissionType> permissions) {
        Set<ManagerPermissionType> requestedPermissions = permissions == null ? Set.of() : permissions;

        assignment.getPermissions().removeIf(existingPermission -> !requestedPermissions.contains(existingPermission.getPermission()));

        Set<ManagerPermissionType> existingTypes = assignment
            .getPermissions()
            .stream()
            .map(ManagerPermission::getPermission)
            .collect(java.util.stream.Collectors.toSet());

        for (ManagerPermissionType permissionType : requestedPermissions) {
            if (existingTypes.contains(permissionType)) {
                continue;
            }

            ManagerPermission permission = new ManagerPermission();

            permission.setAssignment(assignment);

            permission.setPermission(permissionType);

            assignment.getPermissions().add(permission);
        }
    }

    private User requireManager(Long userId) {
        User manager = userRepository
            .findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Manager user not found"));

        boolean hasManagerRole = manager
            .getAuthorities()
            .stream()
            .anyMatch(authority -> AuthoritiesConstants.MANAGER.equals(authority.getName()));

        if (!hasManagerRole) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected user does not have ROLE_MANAGER");
        }

        return manager;
    }

    private User requireClient(Long userId) {
        User client = userRepository
            .findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client user not found"));

        boolean hasUserRole = client
            .getAuthorities()
            .stream()
            .anyMatch(authority -> AuthoritiesConstants.USER.equals(authority.getName()));

        if (!hasUserRole) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected client does not have ROLE_USER");
        }

        return client;
    }

    private void validateDifferentUsers(ManagerAssignmentRequest request) {
        if (request.getManagerId() != null && request.getManagerId().equals(request.getClientUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Manager cannot be assigned to himself");
        }
    }
}
