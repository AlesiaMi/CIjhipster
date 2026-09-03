package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.service.CollectionJobAsyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/collection-job")
public class CollectionJobResource {

    private final CollectionJobAsyncService collectionJobAsyncService;

    public CollectionJobResource(CollectionJobAsyncService collectionJobAsyncService) {
        this.collectionJobAsyncService = collectionJobAsyncService;
    }

    @PostMapping("/run-rss")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> runRssCollection() {
        collectionJobAsyncService.runAsync();

        return ResponseEntity.accepted().build();
    }
}
