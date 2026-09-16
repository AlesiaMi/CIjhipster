package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.enumeration.ManagerPermissionType;
import com.mycompany.myapp.repository.ManagerAssignmentRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import java.util.LinkedHashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class ManagerAccessService {

    private final ManagerAssignmentRepository managerAssignmentRepository;

    public ManagerAccessService(ManagerAssignmentRepository managerAssignmentRepository) {
        this.managerAssignmentRepository = managerAssignmentRepository;
    }

    /**
     * Может ли текущий пользователь вообще видеть
     * бизнес-данные указанного клиента.
     */
    public boolean canView(Long clientUserId) {
        Long currentUserId = requireCurrentUserId();

        if (isAdmin()) {
            return true;
        }

        if (currentUserId.equals(clientUserId)) {
            return true;
        }

        if (!isManager()) {
            return false;
        }

        return managerAssignmentRepository.hasPermission(currentUserId, clientUserId, ManagerPermissionType.VIEW);
    }

    /**
     * Есть ли у текущего пользователя конкретное право
     * на данные указанного клиента.
     *
     * Владелец своих данных имеет полный доступ.
     * ADMIN пока сохраняет текущий глобальный доступ.
     *
     * Для MANAGER обязательно нужны:
     * 1. активное назначение;
     * 2. VIEW;
     * 3. конкретное permission.
     */
    public boolean hasPermission(Long clientUserId, ManagerPermissionType permission) {
        Long currentUserId = requireCurrentUserId();

        if (isAdmin()) {
            return true;
        }

        if (currentUserId.equals(clientUserId)) {
            return true;
        }

        if (!isManager()) {
            return false;
        }

        boolean canView = managerAssignmentRepository.hasPermission(currentUserId, clientUserId, ManagerPermissionType.VIEW);

        if (!canView) {
            return false;
        }

        if (permission == ManagerPermissionType.VIEW) {
            return true;
        }

        return managerAssignmentRepository.hasPermission(currentUserId, clientUserId, permission);
    }

    public boolean isAssignedTo(Long clientUserId) {
        Long currentUserId = requireCurrentUserId();

        if (!isManager()) {
            return false;
        }

        return managerAssignmentRepository.existsByManagerIdAndClientUserIdAndActiveTrue(currentUserId, clientUserId);
    }

    public List<Long> getViewableClientUserIds() {
        Long currentUserId = requireCurrentUserId();

        if (isAdmin()) {
            throw new IllegalStateException("Global admin scope must not be converted to client ids");
        }

        Set<Long> ownerIds = new LinkedHashSet<>();

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.USER)) {
            ownerIds.add(currentUserId);
        }

        if (isManager()) {
            ownerIds.addAll(
                managerAssignmentRepository.findClientUserIdsByManagerIdAndPermission(currentUserId, ManagerPermissionType.VIEW)
            );
        }

        return List.copyOf(ownerIds);
    }

    private Long requireCurrentUserId() {
        return SecurityUtils.getCurrentUserId().orElseThrow(() ->
            new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current user id not found")
        );
    }

    private boolean isAdmin() {
        return SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
    }

    private boolean isManager() {
        return SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER);
    }
}
