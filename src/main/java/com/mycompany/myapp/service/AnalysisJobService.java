package com.mycompany.myapp.service;

import java.util.List;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AnalysisJobService {

    private final GeminiAnalysisService geminiAnalysisService;
    private final TenantCacheVersionService tenantCacheVersionService;

    public AnalysisJobService(GeminiAnalysisService geminiAnalysisService, TenantCacheVersionService tenantCacheVersionService) {
        this.geminiAnalysisService = geminiAnalysisService;
        this.tenantCacheVersionService = tenantCacheVersionService;
    }

    @Async("analysisExecutor")
    public void analyzeBatchAsync(List<Long> newsItemIds, Long ownerId) {
        geminiAnalysisService.analyzeNewsBatch(newsItemIds);

        tenantCacheVersionService.invalidate(ownerId);
    }
}
