package com.mycompany.myapp.service.importer.handler;

import com.mycompany.myapp.domain.Competitor;
import com.mycompany.myapp.domain.DataSource;
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
import com.mycompany.myapp.service.importer.model.ImportContext;
import com.mycompany.myapp.service.importer.model.ImportError;
import com.mycompany.myapp.service.importer.model.ImportRecord;
import com.mycompany.myapp.service.importer.model.XmlImportDefinition;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DataSourceImportHandler extends AbstractEntityImportHandler<DataSource, DataSourceDTO> {

    private static final String ENTITY_TYPE = "data-source";
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
        super(ENTITY_TYPE, XML_DEFINITION);
        this.competitorRepository = competitorRepository;
        this.dataSourceRepository = dataSourceRepository;
        this.dataSourceService = dataSourceService;
        this.managerAccessService = managerAccessService;
    }

    @Override
    protected Class<DataSource> getDomainClass() {
        return DataSource.class;
    }

    @Override
    protected void validateSpecificBusinessRules(List<ImportRecord> records, ImportContext context, List<ImportError> errors) {
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER) && context.clientUserId() == null) {
            errors.add(new ImportError(null, "clientUserId", "clientUserId is required for manager import"));
            return;
        }
        Set<Long> competitorIds = new LinkedHashSet<>();
        Map<Integer, Long> competitorIdByRow = new HashMap<>();
        Map<Integer, String> urlByRow = new HashMap<>();
        for (ImportRecord record : records) {
            Long competitorId = longValue(record, "competitor.id");
            if (competitorId != null && competitorId > 0) {
                competitorIds.add(competitorId);
                competitorIdByRow.put(record.rowNumber(), competitorId);
            }
            String url = stringValue(record, "url");
            if (url != null && validateHttpUrl(record, "url", errors)) {
                urlByRow.put(record.rowNumber(), url);
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
                errors.add(new ImportError(record.rowNumber(), "competitor.id", "Competitor " + competitorId + " not found"));
                continue;
            }

            if (competitor.getOwner() == null || competitor.getOwner().getId() == null) {
                errors.add(new ImportError(record.rowNumber(), "competitor.id", "Competitor has no owner"));
                continue;
            }

            Long ownerId = competitor.getOwner().getId();

            if (!canImportForOwner(ownerId, context)) {
                errors.add(
                    new ImportError(
                        record.rowNumber(),
                        "competitor.id",
                        "Current user has no SOURCES_EDIT access to competitor " + competitorId
                    )
                );
            }

            if (url == null) {
                continue;
            }
            String key = duplicateKey(competitorId, url);
            boolean uniqueInFile = addUniqueKey(
                fileKeys,
                key,
                record,
                "url",
                "Duplicate data source in import file for competitor " + competitorId,
                errors
            );
            if (uniqueInFile && existingKeys.contains(key)) {
                errors.add(
                    new ImportError(record.rowNumber(), "url", "Data source with this URL already exists for competitor " + competitorId)
                );
            }
        }
    }

    private boolean canImportForOwner(Long ownerId, ImportContext context) {
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return context.clientUserId() == null || ownerId.equals(context.clientUserId());
        }
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER)) {
            return (
                ownerId.equals(context.clientUserId()) && managerAccessService.hasPermission(ownerId, ManagerPermissionType.SOURCES_EDIT)
            );
        }
        return SecurityUtils.getCurrentUserId().filter(ownerId::equals).isPresent();
    }

    @Override
    protected DataSourceDTO mapSpecificRecord(ImportRecord record, Instant batchTimestamp) {
        DataSourceDTO dto = new DataSourceDTO();
        dto.setSourceName(stringValue(record, "sourceName"));
        dto.setUrl(stringValue(record, "url"));
        dto.setSourceType(enumValue(record, "sourceType", SourceType.class));
        dto.setIsActive(booleanValue(record, "isActive"));
        dto.setCreatedAt(batchTimestamp);
        dto.setLastCheckedAt(null);
        CompetitorDTO competitor = new CompetitorDTO();
        competitor.setId(longValue(record, "competitor.id"));
        dto.setCompetitor(competitor);
        return dto;
    }

    @Override
    protected List<DataSourceDTO> saveSpecificRecords(List<DataSourceDTO> records, ImportContext context) {
        return dataSourceService.saveImported(records);
    }

    private String duplicateKey(Long competitorId, String url) {
        return competitorId + "|" + url.trim();
    }
}
