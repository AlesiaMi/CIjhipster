package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.service.ManagerClientService;
import com.mycompany.myapp.service.dto.ManagerClientDTO;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manager")
@PreAuthorize("hasAuthority('" + AuthoritiesConstants.MANAGER + "')")
public class ManagerClientResource {

    private final ManagerClientService managerClientService;

    public ManagerClientResource(ManagerClientService managerClientService) {
        this.managerClientService = managerClientService;
    }

    @GetMapping("/clients")
    public ResponseEntity<List<ManagerClientDTO>> getClients() {
        return ResponseEntity.ok(managerClientService.getCurrentManagerClients());
    }
}
