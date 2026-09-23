package com.mycompany.myapp.service.importer.handler;

import com.mycompany.myapp.domain.Competitor;
import com.mycompany.myapp.domain.DataSource;
import com.mycompany.myapp.repository.CompetitorRepository;
import com.mycompany.myapp.repository.DataSourceRepository;
import com.mycompany.myapp.repository.NewsItemRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.NewsItemService;
import com.mycompany.myapp.service.dto.CompetitorDTO;
import com.mycompany.myapp.service.dto.DataSourceDTO;
import com.mycompany.myapp.service.dto.NewsItemDTO;
import com.mycompany.myapp.service.importer.model.ImportContext;
import com.mycompany.myapp.service.importer.model.ImportError;
import com.mycompany.myapp.service.importer.model.ImportRecord;
import com.mycompany.myapp.service.importer.model.XmlImportDefinition;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class NewsItemImportHandler implements EntityImportHandler {

    private static final String ENTITY_TYPE = "news-item";
    private static final Set<String> SUPPORTED_FIELDS = Set.of(
        "externalId",
        "title",
        "url",
        "originalText",
        "publishedAt",
        "dataSourceId",
        "competitorId"
    );

    private static final Set<String> REQUIRED_FIELDS = Set.of("title", "url", "dataSourceId", "competitorId");

    private static final XmlImportDefinition XML_DEFINITION = new XmlImportDefinition(
        "newsItems",
        "newsItem",
        "news-item.dtd",
        "import/dtd/news-item.dtd"
    );

    private final NewsItemRepository newsItemRepository;
    private final DataSourceRepository dataSourceRepository;
    private final CompetitorRepository competitorRepository;
    private final NewsItemService newsItemService;

    public NewsItemImportHandler(
        NewsItemRepository newsItemRepository,
        DataSourceRepository dataSourceRepository,
        CompetitorRepository competitorRepository,
        NewsItemService newsItemService
    ) {
        this.newsItemRepository = newsItemRepository;
        this.dataSourceRepository = dataSourceRepository;
        this.competitorRepository = competitorRepository;
        this.newsItemService = newsItemService;
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
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            errors.add(new ImportError(null, null, "Only administrator can import NewsItem"));
            return List.copyOf(errors);
        }

        Set<String> fileUrls = new HashSet<>();
        Set<String> fileExternalIds = new HashSet<>();

        for (ImportRecord record : records) {
            validateFieldNames(record, errors);
            validateRequiredFields(record, errors);

            validateLength(record, "externalId", 255, errors);
            validateLength(record, "title", 500, errors);
            validateLength(record, "url", 1000, errors);
            validateLength(record, "originalText", 10000, errors);
            validateUrl(record, errors);
            validatePublishedAt(record, errors);
            Long competitorId = parsePositiveLong(record, "competitorId", errors);
            Long dataSourceId = parsePositiveLong(record, "dataSourceId", errors);

            if (competitorId != null && dataSourceId != null) {
                validateRelations(record, competitorId, dataSourceId, errors);
                validateDatabaseDuplicates(record, competitorId, dataSourceId, errors);
            }
            validateFileDuplicates(record, fileUrls, fileExternalIds, errors);
        }

        return List.copyOf(errors);
    }

    @Override
    public int importRecords(List<ImportRecord> records, ImportContext context) {
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            throw new IllegalStateException("Only administrator can import NewsItem");
        }
        Instant collectedAt = Instant.now();
        List<NewsItemDTO> newsItems = records
            .stream()
            .map(record -> toNewsItemDto(record, collectedAt))
            .toList();
        return newsItemService.saveImported(newsItems).size();
    }

    private void validateRelations(ImportRecord record, Long competitorId, Long dataSourceId, List<ImportError> errors) {
        Competitor competitor = competitorRepository.findById(competitorId).orElse(null);

        if (competitor == null) {
            errors.add(new ImportError(record.rowNumber(), "competitorId", "Competitor not found"));
            return;
        }

        DataSource dataSource = dataSourceRepository.findById(dataSourceId).orElse(null);
        if (dataSource == null) {
            errors.add(new ImportError(record.rowNumber(), "dataSourceId", "DataSource not found"));
            return;
        }

        if (dataSource.getCompetitor() == null || !competitorId.equals(dataSource.getCompetitor().getId())) {
            errors.add(new ImportError(record.rowNumber(), "dataSourceId", "DataSource does not belong to selected Competitor"));
        }
    }

    private void validateDatabaseDuplicates(ImportRecord record, Long competitorId, Long dataSourceId, List<ImportError> errors) {
        Competitor competitor = competitorRepository.findById(competitorId).orElse(null);
        if (competitor == null || competitor.getOwner() == null) {
            return;
        }

        Long ownerId = competitor.getOwner().getId();
        String url = trimmed(record, "url");
        if (url != null && newsItemRepository.existsByUrlAndCompetitorOwnerId(url, ownerId)) {
            errors.add(new ImportError(record.rowNumber(), "url", "NewsItem with this URL already exists"));
        }
        String externalId = optionalTrimmed(record, "externalId");
        if (externalId != null && newsItemRepository.existsByExternalIdAndDataSourceId(externalId, dataSourceId)) {
            errors.add(
                new ImportError(record.rowNumber(), "externalId", "NewsItem with this externalId already exists for this DataSource")
            );
        }
    }

    private void validateFileDuplicates(ImportRecord record, Set<String> fileUrls, Set<String> fileExternalIds, List<ImportError> errors) {
        String url = optionalTrimmed(record, "url");
        if (url != null) {
            String normalizedUrl = url.toLowerCase(Locale.ROOT);
            if (!fileUrls.add(normalizedUrl)) {
                errors.add(new ImportError(record.rowNumber(), "url", "Duplicate URL in import file"));
            }
        }
        String externalId = optionalTrimmed(record, "externalId");
        String dataSourceId = optionalTrimmed(record, "dataSourceId");
        if (externalId != null && dataSourceId != null) {
            String key = dataSourceId + "|" + externalId;
            if (!fileExternalIds.add(key)) {
                errors.add(new ImportError(record.rowNumber(), "externalId", "Duplicate externalId for DataSource in import file"));
            }
        }
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
        String value = optionalTrimmed(record, field);

        if (value != null && value.length() > maxLength) {
            errors.add(new ImportError(record.rowNumber(), field, "Maximum length is " + maxLength + " characters"));
        }
    }

    private void validateUrl(ImportRecord record, List<ImportError> errors) {
        String value = optionalTrimmed(record, "url");

        if (value == null) {
            return;
        }
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            if (uri.getHost() == null || scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new URISyntaxException(value, "HTTP/HTTPS URL required");
            }
        } catch (URISyntaxException exception) {
            errors.add(new ImportError(record.rowNumber(), "url", "Invalid HTTP/HTTPS URL"));
        }
    }

    private void validatePublishedAt(ImportRecord record, List<ImportError> errors) {
        String value = optionalTrimmed(record, "publishedAt");
        if (value == null) {
            return;
        }
        try {
            Instant.parse(value);
        } catch (DateTimeParseException exception) {
            errors.add(new ImportError(record.rowNumber(), "publishedAt", "Expected ISO-8601 instant, for example 2026-09-23T09:30:00Z"));
        }
    }

    private Long parsePositiveLong(ImportRecord record, String field, List<ImportError> errors) {
        String value = optionalTrimmed(record, field);
        if (value == null) {
            return null;
        }

        try {
            long parsed = Long.parseLong(value);
            if (parsed <= 0) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException exception) {
            errors.add(new ImportError(record.rowNumber(), field, "Expected positive integer"));
            return null;
        }
    }

    private NewsItemDTO toNewsItemDto(ImportRecord record, Instant collectedAt) {
        NewsItemDTO dto = new NewsItemDTO();
        dto.setExternalId(optionalTrimmed(record, "externalId"));
        dto.setTitle(trimmed(record, "title"));
        dto.setUrl(trimmed(record, "url"));
        dto.setOriginalText(optionalTrimmed(record, "originalText"));
        String publishedAt = optionalTrimmed(record, "publishedAt");
        if (publishedAt != null) {
            dto.setPublishedAt(Instant.parse(publishedAt));
        }

        dto.setCollectedAt(collectedAt);
        dto.setIsDuplicate(false);

        DataSourceDTO dataSource = new DataSourceDTO();
        dataSource.setId(Long.valueOf(trimmed(record, "dataSourceId")));
        dto.setDataSource(dataSource);
        CompetitorDTO competitor = new CompetitorDTO();
        competitor.setId(Long.valueOf(trimmed(record, "competitorId")));
        dto.setCompetitor(competitor);
        dto.setCollectionRun(null);
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
}
