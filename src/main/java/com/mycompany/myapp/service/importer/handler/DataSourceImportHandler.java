package com.mycompany.myapp.service.importer.handler;

import com.mycompany.myapp.domain.Competitor;
import com.mycompany.myapp.domain.enumeration.ManagerPermissionType;
import com.mycompany.myapp.domain.enumeration.SourceType;
import com.mycompany.myapp.repository.CompetitorRepository;
import com.mycompany.myapp.repository.DataSourceRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.DataSourceService;
import com.mycompany.myapp.service.ManagerAccessService;
import com.mycompany.myapp.service.dto.CompetitorDTO;
import com.mycompany.myapp.service.dto.DataSourceDTO;
import com.mycompany.myapp.service.importer.model.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DataSourceImportHandler implements EntityImportHandler {

    private static final String ENTITY_TYPE = "data-source";
    private static final Set<String> SUPPORTED_FIELDS = Set.of("sourceName", "url", "sourceType", "isActive", "competitorId");
    private static final XmlImportDefinition XML_DEFINITION = new XmlImportDefinition(
        "dataSources",
        "dataSource",
        "data-source.dtd",
        "import/dtd/data-source.dtd"
    );
    private final CompetitorRepository competitorRepository;
    private final DataSourceRepository dataSourceRepository;
    private final DataSourceService dataSourceService;
    private final ManagerAccessService managerAccessService;

    public DataSourceImportHandler(
        CompetitorRepository competitorRepository,
        DataSourceRepository dataSourceRepository,
        DataSourceService dataSourceService,
        ManagerAccessService managerAccessService
    ) {
        this.competitorRepository = competitorRepository;
        this.dataSourceRepository = dataSourceRepository;
        this.dataSourceService = dataSourceService;
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
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER) && context.clientUserId() == null) {
            return List.of(new ImportError(null, "clientUserId", "clientUserId is required for manager import"));
        }
        Set<Long> competitorIds = new LinkedHashSet<>();
        Map<Integer, Long> competitorIdByRow = new HashMap<>();
        Map<Integer, String> urlByRow = new HashMap<>();
        for (ImportRecord record : records) {
            validateFieldNames(record, errors);
            validateRequiredFields(record, errors);
            validateSourceName(record, errors);
            String url = validateUrl(record, errors);
            if (url != null) {
                urlByRow.put(record.rowNumber(), url);
            }
            validateSourceType(record, errors);
            validateBoolean(record, "isActive", errors);
            Long competitorId = validateCompetitorId(record, errors);
            if (competitorId != null) {
                competitorIds.add(competitorId);
                competitorIdByRow.put(record.rowNumber(), competitorId);
            }
        }
        Map<Long, Competitor> competitors = competitorRepository
            .findAllById(competitorIds)
            .stream()
            .collect(Collectors.toMap(Competitor::getId, Function.identity()));
        Set<String> existingKeys = competitorIds.isEmpty()
            ? Set.of()
            : dataSourceRepository
                  .findAllByCompetitorIds(competitorIds)
                  .stream()
                  .map(dataSource -> duplicateKey(dataSource.getCompetitor().getId(), dataSource.getUrl()))
                  .collect(Collectors.toSet());
        Set<String> fileKeys = new HashSet<>();
        for (ImportRecord record : records) {
            Long competitorId = competitorIdByRow.get(record.rowNumber());
            String url = urlByRow.get(record.rowNumber());
            if (competitorId == null) {
                continue;
            }
            Competitor competitor = competitors.get(competitorId);
            if (competitor == null) {
                errors.add(new ImportError(record.rowNumber(), "competitorId", "Competitor " + competitorId + " not found"));
                continue;
            }

            if (competitor.getOwner() == null || competitor.getOwner().getId() == null) {
                errors.add(new ImportError(record.rowNumber(), "competitorId", "Competitor has no owner"));
                continue;
            }

            Long ownerId = competitor.getOwner().getId();

            if (!canImportForOwner(ownerId, context)) {
                errors.add(
                    new ImportError(
                        record.rowNumber(),
                        "competitorId",
                        "Current user has no SOURCES_EDIT access to competitor " + competitorId
                    )
                );
            }

            if (url == null) {
                continue;
            }

            String key = duplicateKey(competitorId, url);
            if (!fileKeys.add(key)) {
                errors.add(
                    new ImportError(record.rowNumber(), "url", "Duplicate data source in import file for competitor " + competitorId)
                );
            } else if (existingKeys.contains(key)) {
                errors.add(
                    new ImportError(record.rowNumber(), "url", "Data source with this URL already exists for competitor " + competitorId)
                );
            }
        }

        return List.copyOf(errors);
    }

    @Override
    public int importRecords(List<ImportRecord> records, ImportContext context) {
        Instant createdAt = Instant.now();
        List<DataSourceDTO> dataSources = records
            .stream()
            .map(record -> toDataSourceDto(record, createdAt))
            .toList();

        return dataSourceService.saveImported(dataSources).size();
    }

    private void validateFieldNames(ImportRecord record, List<ImportError> errors) {
        for (String field : record.values().keySet()) {
            if (!SUPPORTED_FIELDS.contains(field)) {
                errors.add(new ImportError(record.rowNumber(), field, "Unknown import field"));
            }
        }
    }

    private void validateRequiredFields(ImportRecord record, List<ImportError> errors) {
        for (String field : SUPPORTED_FIELDS) {
            String value = record.values().get(field);
            if (value == null || value.isBlank()) {
                errors.add(new ImportError(record.rowNumber(), field, "Field is required"));
            }
        }
    }

    private void validateSourceName(ImportRecord record, List<ImportError> errors) {
        String value = trimmed(record, "sourceName");
        if (value != null && !value.isEmpty() && value.length() > 255) {
            errors.add(new ImportError(record.rowNumber(), "sourceName", "Maximum length is 255 characters"));
        }
    }

    private String validateUrl(ImportRecord record, List<ImportError> errors) {
        String value = trimmed(record, "url");
        if (value == null || value.isEmpty()) {
            return null;
        }
        if (value.length() > 1000) {
            errors.add(new ImportError(record.rowNumber(), "url", "Maximum length is 1000 characters"));
            return null;
        }

        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) || uri.getHost() == null) {
                throw new URISyntaxException(value, "HTTP or HTTPS URL with host is required");
            }
        } catch (URISyntaxException exception) {
            errors.add(new ImportError(record.rowNumber(), "url", "Invalid HTTP/HTTPS URL"));
            return null;
        }
        return value;
    }

    private void validateSourceType(ImportRecord record, List<ImportError> errors) {
        String value = trimmed(record, "sourceType");
        if (value == null || value.isEmpty()) {
            return;
        }

        try {
            SourceType.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            errors.add(new ImportError(record.rowNumber(), "sourceType", "Allowed values: RSS, WEBSITE, API"));
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

    private Long validateCompetitorId(ImportRecord record, List<ImportError> errors) {
        String value = trimmed(record, "competitorId");
        if (value == null || value.isEmpty()) {
            return null;
        }

        try {
            long competitorId = Long.parseLong(value);
            if (competitorId <= 0) {
                throw new NumberFormatException();
            }
            return competitorId;
        } catch (NumberFormatException exception) {
            errors.add(new ImportError(record.rowNumber(), "competitorId", "competitorId must be a positive integer"));
            return null;
        }
    }

    private boolean canImportForOwner(Long ownerId, ImportContext context) {
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return (context.clientUserId() == null || ownerId.equals(context.clientUserId()));
        }
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER)) {
            return (
                ownerId.equals(context.clientUserId()) && managerAccessService.hasPermission(ownerId, ManagerPermissionType.SOURCES_EDIT)
            );
        }
        return managerAccessService.hasPermission(ownerId, ManagerPermissionType.SOURCES_EDIT);
    }

    private DataSourceDTO toDataSourceDto(ImportRecord record, Instant createdAt) {
        DataSourceDTO dto = new DataSourceDTO();
        dto.setSourceName(trimmed(record, "sourceName"));
        dto.setUrl(trimmed(record, "url"));
        dto.setSourceType(SourceType.valueOf(trimmed(record, "sourceType").toUpperCase(Locale.ROOT)));
        dto.setIsActive(Boolean.valueOf(trimmed(record, "isActive")));
        dto.setCreatedAt(createdAt);
        dto.setLastCheckedAt(null);
        CompetitorDTO competitor = new CompetitorDTO();
        competitor.setId(Long.valueOf(trimmed(record, "competitorId")));
        dto.setCompetitor(competitor);
        return dto;
    }

    private String trimmed(ImportRecord record, String field) {
        String value = record.values().get(field);
        return value == null ? null : value.trim();
    }

    private String duplicateKey(Long competitorId, String url) {
        return competitorId + "|" + url.trim();
    }
}
