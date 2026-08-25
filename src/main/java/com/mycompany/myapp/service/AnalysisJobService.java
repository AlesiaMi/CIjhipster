package com.mycompany.myapp.service;

import java.util.List;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AnalysisJobService {

    private final GeminiAnalysisService geminiAnalysisService;

    public AnalysisJobService(GeminiAnalysisService geminiAnalysisService) {
        this.geminiAnalysisService = geminiAnalysisService;
    }

    @Async("analysisExecutor")
    public void analyzeBatchAsync(List<Long> newsItemIds) {
        geminiAnalysisService.analyzeNewsBatch(newsItemIds);
    }

    /*
     * Пока оставляем старый метод.
     * Может использоваться где-нибудь ещё в приложении.
     */
    @Async("analysisExecutor")
    public void analyzeAsync(Long newsItemId) {
        geminiAnalysisService.analyzeNewsById(newsItemId);
    }
}
