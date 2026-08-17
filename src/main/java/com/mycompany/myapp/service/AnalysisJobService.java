package com.mycompany.myapp.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AnalysisJobService {

    private final GeminiAnalysisService geminiAnalysisService;

    public AnalysisJobService(GeminiAnalysisService geminiAnalysisService) {
        this.geminiAnalysisService = geminiAnalysisService;
    }

    @Async("analysisExecutor")
    public void analyzeAsync(Long newsItemId) {
        geminiAnalysisService.analyzeNewsById(newsItemId);
    }
}
