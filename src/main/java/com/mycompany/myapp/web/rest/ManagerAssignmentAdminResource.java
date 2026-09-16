package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.service.ManagerAssignmentAdminService;
import com.mycompany.myapp.service.dto.ManagerAssignmentDTO;
import com.mycompany.myapp.service.dto.ManagerAssignmentRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/manager-assignments")
@PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "')")
public class ManagerAssignmentAdminResource {

    private final ManagerAssignmentAdminService managerAssignmentAdminService;

    public ManagerAssignmentAdminResource(ManagerAssignmentAdminService managerAssignmentAdminService) {
        this.managerAssignmentAdminService = managerAssignmentAdminService;
    }

    @GetMapping
    public ResponseEntity<List<ManagerAssignmentDTO>> getAll() {
        return ResponseEntity.ok(managerAssignmentAdminService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ManagerAssignmentDTO> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(managerAssignmentAdminService.findOne(id));
    }

    @PostMapping
    public ResponseEntity<ManagerAssignmentDTO> create(@Valid @RequestBody ManagerAssignmentRequest request) throws URISyntaxException {
        ManagerAssignmentDTO result = managerAssignmentAdminService.create(request);

        return ResponseEntity.created(new URI("/api/admin/manager-assignments/" + result.getId())).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ManagerAssignmentDTO> update(@PathVariable Long id, @Valid @RequestBody ManagerAssignmentRequest request) {
        return ResponseEntity.ok(managerAssignmentAdminService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        managerAssignmentAdminService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
