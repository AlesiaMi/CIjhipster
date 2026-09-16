package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.service.AdminDashboardService;
import com.mycompany.myapp.service.dto.AdminDashboardDTO;
import com.mycompany.myapp.service.dto.AdminUserDashboardDTO;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ci-dashboard")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminDashboardResource {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardResource(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("")
    public ResponseEntity<AdminDashboardDTO> getDashboard() {
        return ResponseEntity.ok(adminDashboardService.getDashboard());
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminDashboardDTO.UserSummaryDTO>> getUsers() {
        return ResponseEntity.ok(adminDashboardService.getUsers());
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<AdminUserDashboardDTO> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminDashboardService.getUserDashboard(userId));
    }
}
