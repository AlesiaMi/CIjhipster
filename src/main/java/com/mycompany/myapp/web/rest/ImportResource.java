package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.service.importer.ImportService;
import com.mycompany.myapp.service.importer.model.ImportResult;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/import")
public class ImportResource {

    private static final Logger LOG = LoggerFactory.getLogger(ImportResource.class);

    private final ImportService importService;

    public ImportResource(ImportService importService) {
        this.importService = importService;
    }

    @GetMapping("/supported-entities")
    public ResponseEntity<Set<String>> getSupportedEntities() {
        return ResponseEntity.ok(importService.supportedEntityTypes());
    }

    @PostMapping(value = "/{entityType}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResult> importEntity(
        @PathVariable String entityType,
        @RequestParam("file") MultipartFile file,
        @RequestParam(name = "clientUserId", required = false) Long clientUserId
    ) {
        LOG.debug("REST request to import entity type {} from file {}", entityType, file.getOriginalFilename());

        ImportResult result = importService.importFile(entityType, file, clientUserId);

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result);
        }

        return ResponseEntity.ok(result);
    }
}
