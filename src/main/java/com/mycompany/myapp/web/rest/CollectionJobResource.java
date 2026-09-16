package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.domain.enumeration.ManagerPermissionType;
import com.mycompany.myapp.domain.enumeration.RunStatus;
import com.mycompany.myapp.repository.CollectionRunRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.CollectionJobAsyncService;
import com.mycompany.myapp.service.ManagerAccessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/collection-job")
public class CollectionJobResource {

    private final CollectionJobAsyncService collectionJobAsyncService;

    private final CollectionRunRepository collectionRunRepository;

    private final ManagerAccessService managerAccessService;

    public CollectionJobResource(
        CollectionJobAsyncService collectionJobAsyncService,
        CollectionRunRepository collectionRunRepository,
        ManagerAccessService managerAccessService
    ) {
        this.collectionJobAsyncService = collectionJobAsyncService;

        this.collectionRunRepository = collectionRunRepository;

        this.managerAccessService = managerAccessService;
    }

    @PostMapping("/run-rss")
    public ResponseEntity<Void> runRssCollection(@RequestParam(name = "clientUserId", required = false) Long clientUserId) {
        Long ownerId = resolveCollectionOwnerId(clientUserId);

        if (collectionRunRepository.existsByOwnerIdAndStatus(ownerId, RunStatus.RUNNING)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "RSS collection is already running for this user");
        }

        collectionJobAsyncService.runAsync(ownerId);

        return ResponseEntity.accepted().build();
    }

    private Long resolveCollectionOwnerId(Long clientUserId) {
        Long currentUserId = SecurityUtils.getCurrentUserId().orElseThrow(() ->
            new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current user id not found")
        );

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER)) {
            if (clientUserId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientUserId is required for manager");
            }

            if (!managerAccessService.hasPermission(clientUserId, ManagerPermissionType.COLLECTION_RUN)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "COLLECTION_RUN permission is required");
            }

            return clientUserId;
        }

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN) && clientUserId != null) {
            return clientUserId;
        }

        return currentUserId;
    }
}
