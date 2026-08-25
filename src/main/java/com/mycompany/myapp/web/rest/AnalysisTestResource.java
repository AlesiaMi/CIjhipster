package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.service.GeminiAnalysisService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analysis-test")
public class AnalysisTestResource {

    private final GeminiAnalysisService geminiAnalysisService;

    public AnalysisTestResource(GeminiAnalysisService geminiAnalysisService) {
        this.geminiAnalysisService = geminiAnalysisService;
    }

    /**
     * Сравнение JSON и TOON для одной новости.
     */
    @PostMapping("/compare/{newsItemId}")
    public ResponseEntity<String> compareFormats(@PathVariable Long newsItemId) {
        geminiAnalysisService.compareFormats(newsItemId);

        return ResponseEntity.ok("JSON vs TOON comparison completed for NewsItem ID: " + newsItemId);
    }

    /**
     * Сравнение JSON и TOON для нескольких новостей.
     *
     * Пример body:
     * [1156, 1157, 1158, 1159]
     */
    @PostMapping("/compare-batch")
    public ResponseEntity<String> compareFormatsBatch(@RequestBody List<Long> newsItemIds) {
        geminiAnalysisService.compareFormatsBatch(newsItemIds);

        return ResponseEntity.ok("JSON vs TOON batch comparison completed for " + newsItemIds.size() + " NewsItems");
    }

    @PostMapping("/compare-single-batch")
    public ResponseEntity<String> compareFormatsSingleBatch(@RequestBody List<Long> newsItemIds) {
        geminiAnalysisService.compareFormatsSingleBatch(newsItemIds);

        return ResponseEntity.ok("JSON vs TOON single batch comparison completed for " + newsItemIds.size() + " NewsItems");
    }

    @PostMapping("/compare-detailed-batch")
    public ResponseEntity<String> compareFormatsDetailedBatch(@RequestBody List<Long> newsItemIds) {
        geminiAnalysisService.compareFormatsDetailedBatch(newsItemIds);

        return ResponseEntity.ok("Detailed JSON vs TOON comparison completed for " + newsItemIds.size() + " NewsItems");
    }
}
