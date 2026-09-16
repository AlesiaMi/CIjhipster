package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.DashboardService;
import com.mycompany.myapp.service.ManagerAccessService;
import com.mycompany.myapp.service.dto.DashboardDTO;
import com.mycompany.myapp.service.dto.NewsItemDTO;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardResource {

    private final DashboardService dashboardService;
    private final ManagerAccessService managerAccessService;

    public DashboardResource(DashboardService dashboardService, ManagerAccessService managerAccessService) {
        this.dashboardService = dashboardService;
        this.managerAccessService = managerAccessService;
    }

    @GetMapping("")
    public ResponseEntity<DashboardDTO> getDashboard(@RequestParam(name = "clientUserId", required = false) Long clientUserId) {
        Long ownerId = resolveDashboardOwnerId(clientUserId);

        return ResponseEntity.ok(dashboardService.getDashboard(ownerId));
    }

    @GetMapping("/latest-news")
    public ResponseEntity<List<NewsItemDTO>> getLatestNews(@RequestParam(name = "clientUserId", required = false) Long clientUserId) {
        Long ownerId = resolveDashboardOwnerId(clientUserId);

        return ResponseEntity.ok(dashboardService.getLatestFiveNews(ownerId));
    }

    @GetMapping("/news-count")
    public ResponseEntity<Long> getNewsCount(@RequestParam(name = "clientUserId", required = false) Long clientUserId) {
        Long ownerId = resolveDashboardOwnerId(clientUserId);

        return ResponseEntity.ok(dashboardService.getNewsCount(ownerId));
    }

    private Long resolveDashboardOwnerId(Long clientUserId) {
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            // Сохраняем существующий global dashboard admin.
            return null;
        }

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER)) {
            if (clientUserId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientUserId is required for manager");
            }

            if (!managerAccessService.canView(clientUserId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Manager has no access to this client");
            }

            return clientUserId;
        }

        // Обычный пользователь никогда не может
        // подменить owner через query parameter.
        return SecurityUtils.getCurrentUserId().orElseThrow(() ->
            new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current user id not found")
        );
    }
}
