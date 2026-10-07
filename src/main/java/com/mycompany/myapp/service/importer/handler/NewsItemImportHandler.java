package com.mycompany.myapp.service.importer.handler;

import com.mycompany.myapp.domain.Competitor;
import com.mycompany.myapp.domain.DataSource;
import com.mycompany.myapp.domain.NewsItem;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class NewsItemImportHandler extends AbstractEntityImportHandler<NewsItem, NewsItemDTO> {

    private static final String ENTITY_TYPE = "news-item";
    private static final int BULK_LOOKUP_CHUNK_SIZE = 1000;
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
        super(ENTITY_TYPE, XML_DEFINITION);
        this.newsItemRepository = newsItemRepository;
        this.dataSourceRepository = dataSourceRepository;
        this.competitorRepository = competitorRepository;
        this.newsItemService = newsItemService;
    }

    @Override
    protected Class<NewsItem> getDomainClass() {
        return NewsItem.class;
    }

    @Override
    protected void validateSpecificBusinessRules(List<ImportRecord> records, ImportContext context, List<ImportError> errors) {
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            errors.add(new ImportError(null, null, "Only administrator can import NewsItem"));
            return;
        }

        Set<Long> competitorIds = new LinkedHashSet<>();
        Set<Long> dataSourceIds = new LinkedHashSet<>();
        Set<String> urls = new LinkedHashSet<>();
        Set<String> externalIds = new LinkedHashSet<>();

        for (ImportRecord record : records) {
            Long competitorId = longValue(record, "competitor.id");
            Long dataSourceId = longValue(record, "dataSource.id");

            if (competitorId == null || competitorId <= 0 || dataSourceId == null || dataSourceId <= 0) {
                continue;
            }
            competitorIds.add(competitorId);
            dataSourceIds.add(dataSourceId);
            String url = stringValue(record, "url");
            if (url != null) {
                urls.add(url);
            }

            String externalId = stringValue(record, "externalId");
            if (externalId != null) {
                externalIds.add(externalId);
            }
        }

        Map<Long, Competitor> competitors = competitorIds.isEmpty()
            ? Map.of()
            : competitorRepository
                  .findAllByIdsWithOwner(competitorIds)
                  .stream()
                  .collect(Collectors.toMap(Competitor::getId, Function.identity()));
        Map<Long, DataSource> dataSources = dataSourceIds.isEmpty()
            ? Map.of()
            : dataSourceRepository
                  .findAllByIdsWithCompetitor(dataSourceIds)
                  .stream()
                  .collect(Collectors.toMap(DataSource::getId, Function.identity()));

        Set<String> existingUrlOwnerKeys = loadExistingUrlOwnerKeys(urls);
        Set<String> existingExternalIdDataSourceKeys = loadExistingExternalIdDataSourceKeys(externalIds);
        Set<String> fileUrls = new HashSet<>();
        Set<String> fileExternalIds = new HashSet<>();

        for (ImportRecord record : records) {
            validateHttpUrl(record, "url", errors);
            Long competitorId = longValue(record, "competitor.id");
            Long dataSourceId = longValue(record, "dataSource.id");

            if (competitorId != null && competitorId > 0 && dataSourceId != null && dataSourceId > 0) {
                Competitor competitor = competitors.get(competitorId);
                DataSource dataSource = dataSources.get(dataSourceId);
                validateRelations(record, competitorId, competitor, dataSource, errors);
                validateDatabaseDuplicates(
                    record,
                    competitor,
                    dataSourceId,
                    existingUrlOwnerKeys,
                    existingExternalIdDataSourceKeys,
                    errors
                );
            }

            validateFileDuplicates(record, fileUrls, fileExternalIds, errors);
        }
    }

    private void validateRelations(
        ImportRecord record,
        Long competitorId,
        Competitor competitor,
        DataSource dataSource,
        List<ImportError> errors
    ) {
        if (competitor == null) {
            errors.add(new ImportError(record.rowNumber(), "competitor.id", "Competitor not found"));
            return;
        }

        if (dataSource == null) {
            errors.add(new ImportError(record.rowNumber(), "dataSource.id", "DataSource not found"));
            return;
        }

        if (dataSource.getCompetitor() == null || !competitorId.equals(dataSource.getCompetitor().getId())) {
            errors.add(new ImportError(record.rowNumber(), "dataSource.id", "DataSource does not belong to selected Competitor"));
        }
    }

    private void validateDatabaseDuplicates(
        ImportRecord record,
        Competitor competitor,
        Long dataSourceId,
        Set<String> existingUrlOwnerKeys,
        Set<String> existingExternalIdDataSourceKeys,
        List<ImportError> errors
    ) {
        if (competitor == null || competitor.getOwner() == null || competitor.getOwner().getId() == null) {
            return;
        }

        Long ownerId = competitor.getOwner().getId();
        String url = stringValue(record, "url");

        if (url != null && existingUrlOwnerKeys.contains(urlOwnerKey(ownerId, url))) {
            errors.add(new ImportError(record.rowNumber(), "url", "NewsItem with this URL already exists"));
        }

        String externalId = stringValue(record, "externalId");

        if (externalId != null && existingExternalIdDataSourceKeys.contains(externalIdDataSourceKey(dataSourceId, externalId))) {
            errors.add(
                new ImportError(record.rowNumber(), "externalId", "NewsItem with this externalId already exists for this DataSource")
            );
        }
    }

    private Set<String> loadExistingUrlOwnerKeys(Set<String> urls) {
        if (urls.isEmpty()) {
            return Set.of();
        }

        Set<String> result = new HashSet<>();
        List<String> values = new ArrayList<>(urls);

        for (int from = 0; from < values.size(); from += BULK_LOOKUP_CHUNK_SIZE) {
            int to = Math.min(from + BULK_LOOKUP_CHUNK_SIZE, values.size());
            newsItemRepository
                .findExistingUrlOwnerPairs(values.subList(from, to))
                .forEach(pair -> result.add(urlOwnerKey(pair.getOwnerId(), pair.getUrl())));
        }
        return result;
    }

    private Set<String> loadExistingExternalIdDataSourceKeys(Set<String> externalIds) {
        if (externalIds.isEmpty()) {
            return Set.of();
        }
        Set<String> result = new HashSet<>();
        List<String> values = new ArrayList<>(externalIds);

        for (int from = 0; from < values.size(); from += BULK_LOOKUP_CHUNK_SIZE) {
            int to = Math.min(from + BULK_LOOKUP_CHUNK_SIZE, values.size());

            newsItemRepository
                .findExistingExternalIdDataSourcePairs(values.subList(from, to))
                .forEach(pair -> result.add(externalIdDataSourceKey(pair.getDataSourceId(), pair.getExternalId())));
        }
        return result;
    }

    private String urlOwnerKey(Long ownerId, String url) {
        return ownerId + "|" + url;
    }

    private String externalIdDataSourceKey(Long dataSourceId, String externalId) {
        return dataSourceId + "|" + externalId;
    }

    private void validateFileDuplicates(ImportRecord record, Set<String> fileUrls, Set<String> fileExternalIds, List<ImportError> errors) {
        String url = stringValue(record, "url");
        if (url != null) {
            String normalizedUrl = normalizeKey(url);
            addUniqueKey(fileUrls, normalizedUrl, record, "url", "Duplicate URL in import file", errors);
        }

        String externalId = stringValue(record, "externalId");
        Long dataSourceId = longValue(record, "dataSource.id");

        if (externalId != null && dataSourceId != null && dataSourceId > 0) {
            String key = dataSourceId + "|" + externalId;
            addUniqueKey(fileExternalIds, key, record, "externalId", "Duplicate externalId for DataSource in import file", errors);
        }
    }

    @Override
    protected NewsItemDTO mapSpecificRecord(ImportRecord record, Instant batchTimestamp) {
        NewsItemDTO dto = new NewsItemDTO();
        dto.setExternalId(stringValue(record, "externalId"));
        dto.setTitle(stringValue(record, "title"));
        dto.setUrl(stringValue(record, "url"));
        dto.setOriginalText(stringValue(record, "originalText"));
        dto.setPublishedAt(instantValue(record, "publishedAt"));
        dto.setCollectedAt(batchTimestamp);
        dto.setIsDuplicate(false);

        DataSourceDTO dataSource = new DataSourceDTO();
        dataSource.setId(longValue(record, "dataSource.id"));
        dto.setDataSource(dataSource);
        CompetitorDTO competitor = new CompetitorDTO();
        competitor.setId(longValue(record, "competitor.id"));
        dto.setCompetitor(competitor);
        dto.setCollectionRun(null);

        return dto;
    }

    @Override
    protected List<NewsItemDTO> saveSpecificRecords(List<NewsItemDTO> records, ImportContext context) {
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            throw new IllegalStateException("Only administrator can import NewsItem");
        }

        return newsItemService.saveImported(records);
    }

    @Override
    protected int saveSpecificRecordsBatch(List<NewsItemDTO> records, ImportContext context) {
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            throw new IllegalStateException("Only administrator can import NewsItem");
        }

        return newsItemService.saveImportedBatch(records);
    }
}
