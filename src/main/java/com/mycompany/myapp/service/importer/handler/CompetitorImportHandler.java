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
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class CompetitorImportHandler extends AbstractEntityImportHandler<Competitor, CompetitorDTO> {

    private static final String ENTITY_TYPE = "competitor";
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
        super(ENTITY_TYPE, XML_DEFINITION);
        this.competitorRepository = competitorRepository;
        this.competitorService = competitorService;
        this.managerAccessService = managerAccessService;
    }

    @Override
    protected Class<Competitor> getDomainClass() {
        return Competitor.class;
    }

    @Override
    protected void validateSpecificBusinessRules(List<ImportRecord> records, ImportContext context, List<ImportError> errors) {
        Long ownerId = resolveOwnerId(context, errors);
        if (ownerId == null) {
            return;
        }
        Set<String> existingNames = new HashSet<>();
        for (Competitor competitor : competitorRepository.findAllByOwnerId(ownerId)) {
            if (competitor.getCompetitorName() != null) {
                existingNames.add(normalizeKey(competitor.getCompetitorName()));
            }
        }

        Set<String> namesInFile = new HashSet<>();

        for (ImportRecord record : records) {
            String competitorName = stringValue(record, "competitorName");
            if (competitorName == null) {
                continue;
            }
            String normalizedName = normalizeKey(competitorName);
            boolean uniqueInFile = addUniqueKey(
                namesInFile,
                normalizedName,
                record,
                "competitorName",
                "Duplicate competitor in import file",
                errors
            );
            if (!uniqueInFile) {
                continue;
            }

            if (existingNames.contains(normalizedName)) {
                errors.add(
                    new ImportError(record.rowNumber(), "competitorName", "Competitor with this name already exists for this owner")
                );
            }
        }
    }

    @Override
    protected CompetitorDTO mapSpecificRecord(ImportRecord record, Instant batchTimestamp) {
        CompetitorDTO dto = new CompetitorDTO();
        dto.setCompetitorName(stringValue(record, "competitorName"));
        dto.setWebsiteUrl(stringValue(record, "websiteUrl"));
        dto.setIndustry(stringValue(record, "industry"));
        dto.setDescription(stringValue(record, "description"));
        dto.setIsActive(booleanValue(record, "isActive"));
        return dto;
    }

    @Override
    protected List<CompetitorDTO> saveSpecificRecords(List<CompetitorDTO> records, ImportContext context) {
        return competitorService.saveImported(records, context.clientUserId());
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
}
