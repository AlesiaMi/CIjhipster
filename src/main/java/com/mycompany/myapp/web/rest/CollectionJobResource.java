package com.mycompany.myapp.web.rest;

//import com.mycompany.myapp.service.CollectionJobService;
//import com.mycompany.myapp.service.CollectionJobService.CollectionJobResult;
import com.mycompany.myapp.service.CollectionJobAsyncService;
import com.mycompany.myapp.service.CollectionJobService;
import com.mycompany.myapp.service.CollectionJobService.CollectionJobResult;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/collection-job")
public class CollectionJobResource {

    //private final CollectionJobService collectionJobService;
    private final CollectionJobAsyncService collectionJobAsyncService;

    /*public CollectionJobResource(CollectionJobService collectionJobService) {
        this.collectionJobService = collectionJobService;
    }*/
    public CollectionJobResource(CollectionJobAsyncService collectionJobAsyncService) {
        this.collectionJobAsyncService = collectionJobAsyncService;
    }

    /* @PostMapping("/run-rss")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CollectionJobResult> runRssCollection() {
        return ResponseEntity.ok(collectionJobService.runRssCollection());
    }*/

    @PostMapping("/run-rss")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> runRssCollection() {
        collectionJobAsyncService.runAsync();

        return ResponseEntity.accepted().build();
    }
}
