package com.mycompany.myapp.service.importer.handler;

import com.mycompany.myapp.domain.Competitor;
import com.mycompany.myapp.domain.enumeration.ManagerPermissionType;
import com.mycompany.myapp.repository.CompetitorRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.CompetitorService;
import com.mycompany.myapp.service.ManagerAccessService;
import com.mycompany.myapp.service.dto.CompetitorDTO;
import com.mycompany.myapp.service.importer.model.ImportContext;
import com.mycompany.myapp.service.importer.model.ImportError;
import com.mycompany.myapp.service.importer.model.ImportRecord;
import com.mycompany.myapp.service.importer.model.XmlImportDefinition;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class CompetitorImportHandler implements EntityImportHandler {

    private static final String ENTITY_TYPE = "competitor";

    private static final Set<String> SUPPORTED_FIELDS = Set.of("competitorName", "websiteUrl", "industry", "description", "isActive");

    private static final Set<String> REQUIRED_FIELDS = Set.of("competitorName", "isActive");

    private static final XmlImportDefinition XML_DEFINITION = new XmlImportDefinition(
        "competitors",
        "competitor",
        "competitor.dtd",
        "import/dtd/competitor.dtd"
    );

    private final CompetitorRepository competitorRepository;
    private final CompetitorService competitorService;
    private final ManagerAccessService managerAccessService;

    public CompetitorImportHandler(
        CompetitorRepository competitorRepository,
        CompetitorService competitorService,
        ManagerAccessService managerAccessService
    ) {
        this.competitorRepository = competitorRepository;
        this.competitorService = competitorService;
        this.managerAccessService = managerAccessService;
    }

    @Override
    public String entityType() {
        return ENTITY_TYPE;
    }

    @Override
    public XmlImportDefinition xmlDefinition() {
        return XML_DEFINITION;
    }

    @Override
    public List<ImportError> validate(List<ImportRecord> records, ImportContext context) {
        List<ImportError> errors = new ArrayList<>();
        Long ownerId = resolveOwnerId(context, errors);
        if (ownerId == null) {
            return List.copyOf(errors);
        }

        for (ImportRecord record : records) {
            validateFieldNames(record, errors);
            validateRequiredFields(record, errors);

            validateLength(record, "competitorName", 255, errors);
            validateLength(record, "websiteUrl", 500, errors);
            validateLength(record, "industry", 255, errors);
            validateLength(record, "description", 2000, errors);
            validateBoolean(record, "isActive", errors);
        }
        Set<String> existingNames = new HashSet<>();
        for (Competitor competitor : competitorRepository.findAllByOwnerId(ownerId)) {
            if (competitor.getCompetitorName() != null) {
                existingNames.add(normalizeName(competitor.getCompetitorName()));
            }
        }

        Set<String> namesInFile = new HashSet<>();
        for (ImportRecord record : records) {
            String competitorName = trimmed(record, "competitorName");

            if (competitorName == null || competitorName.isEmpty()) {
                continue;
            }
            String normalizedName = normalizeName(competitorName);
            if (!namesInFile.add(normalizedName)) {
                errors.add(new ImportError(record.rowNumber(), "competitorName", "Duplicate competitor in import file"));
                continue;
            }
            if (existingNames.contains(normalizedName)) {
                errors.add(
                    new ImportError(record.rowNumber(), "competitorName", "Competitor with this name already exists for this owner")
                );
            }
        }

        return List.copyOf(errors);
    }

    @Override
    public int importRecords(List<ImportRecord> records, ImportContext context) {
        List<CompetitorDTO> competitors = records.stream().map(this::toCompetitorDto).toList();
        return competitorService.saveImported(competitors, context.clientUserId()).size();
    }

    private Long resolveOwnerId(ImportContext context, List<ImportError> errors) {
        Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        if (currentUserId == null) {
            errors.add(new ImportError(null, null, "Current user id not found"));
            return null;
        }
        Long requestedOwnerId = context.clientUserId();
        if (requestedOwnerId == null) {
            if (
                SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER) &&
                !SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.USER)
            ) {
                errors.add(new ImportError(null, "clientUserId", "clientUserId is required for manager import"));
                return null;
            }
            return currentUserId;
        }

        if (requestedOwnerId.equals(currentUserId)) {
            return currentUserId;
        }

        if (managerAccessService.hasPermission(requestedOwnerId, ManagerPermissionType.COMPETITORS_EDIT)) {
            return requestedOwnerId;
        }

        errors.add(new ImportError(null, "clientUserId", "COMPETITORS_EDIT permission is required"));
        return null;
    }

    private void validateFieldNames(ImportRecord record, List<ImportError> errors) {
        for (String field : record.values().keySet()) {
            if (!SUPPORTED_FIELDS.contains(field)) {
                errors.add(new ImportError(record.rowNumber(), field, "Unknown import field"));
            }
        }
    }

    private void validateRequiredFields(ImportRecord record, List<ImportError> errors) {
        for (String field : REQUIRED_FIELDS) {
            String value = record.values().get(field);
            if (value == null || value.isBlank()) {
                errors.add(new ImportError(record.rowNumber(), field, "Field is required"));
            }
        }
    }

    private void validateLength(ImportRecord record, String field, int maxLength, List<ImportError> errors) {
        String value = trimmed(record, field);
        if (value != null && value.length() > maxLength) {
            errors.add(new ImportError(record.rowNumber(), field, "Maximum length is " + maxLength + " characters"));
        }
    }

    private void validateBoolean(ImportRecord record, String field, List<ImportError> errors) {
        String value = trimmed(record, field);
        if (value == null || value.isEmpty()) {
            return;
        }

        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            errors.add(new ImportError(record.rowNumber(), field, "Allowed values: true, false"));
        }
    }

    private CompetitorDTO toCompetitorDto(ImportRecord record) {
        CompetitorDTO dto = new CompetitorDTO();
        dto.setCompetitorName(trimmed(record, "competitorName"));
        dto.setWebsiteUrl(optionalTrimmed(record, "websiteUrl"));
        dto.setIndustry(optionalTrimmed(record, "industry"));
        dto.setDescription(optionalTrimmed(record, "description"));
        dto.setIsActive(Boolean.valueOf(trimmed(record, "isActive")));
        return dto;
    }

    private String trimmed(ImportRecord record, String field) {
        String value = record.values().get(field);
        return value == null ? null : value.trim();
    }

    private String optionalTrimmed(ImportRecord record, String field) {
        String value = trimmed(record, field);
        return (value == null || value.isEmpty()) ? null : value;
    }

    private String normalizeName(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
