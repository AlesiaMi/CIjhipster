package com.mycompany.myapp.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CollectionJobAsyncService {

    private final CollectionJobService collectionJobService;

    public CollectionJobAsyncService(CollectionJobService collectionJobService) {
        this.collectionJobService = collectionJobService;
    }

    @Async("taskExecutor")
    public void runAsync(Long userId) {
        collectionJobService.runRssCollection(userId);
    }
}
