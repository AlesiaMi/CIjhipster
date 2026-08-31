package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.service.DashboardService;
import com.mycompany.myapp.service.dto.DashboardDTO;
import com.mycompany.myapp.service.dto.NewsItemDTO;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardResource {

    private final DashboardService dashboardService;

    public DashboardResource(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("")
    public ResponseEntity<DashboardDTO> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

    @GetMapping("/latest-news")
    public ResponseEntity<List<NewsItemDTO>> getLatestNews() {
        List<NewsItemDTO> latestNews = dashboardService.getLatestFiveNews();

        return ResponseEntity.ok(latestNews);
    }

    @GetMapping("/news-count")
    public ResponseEntity<Long> getNewsCount() {
        long count = dashboardService.getNewsCount();

        return ResponseEntity.ok(count);
    }
}
