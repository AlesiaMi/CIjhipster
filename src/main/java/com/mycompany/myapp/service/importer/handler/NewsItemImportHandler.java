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
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class NewsItemImportHandler extends AbstractEntityImportHandler<NewsItem, NewsItemDTO> {

    private static final String ENTITY_TYPE = "news-item";
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
    protected List<ImportError> validateBusinessRules(List<ImportRecord> records, ImportContext context) {
        List<ImportError> errors = new ArrayList<>();
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            errors.add(new ImportError(null, null, "Only administrator can import NewsItem"));
            return List.copyOf(errors);
        }
        Set<String> fileUrls = new HashSet<>();
        Set<String> fileExternalIds = new HashSet<>();
        for (ImportRecord record : records) {
            validateHttpUrl(record, "url", errors);
            Long competitorId = longValue(record, "competitorId");
            Long dataSourceId = longValue(record, "dataSourceId");
            if (competitorId != null && competitorId > 0 && dataSourceId != null && dataSourceId > 0) {
                validateRelations(record, competitorId, dataSourceId, errors);
                validateDatabaseDuplicates(record, competitorId, dataSourceId, errors);
            }
            validateFileDuplicates(record, fileUrls, fileExternalIds, errors);
        }

        return List.copyOf(errors);
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
        String url = stringValue(record, "url");
        if (url != null && newsItemRepository.existsByUrlAndCompetitorOwnerId(url, ownerId)) {
            errors.add(new ImportError(record.rowNumber(), "url", "NewsItem with this URL already exists"));
        }
        String externalId = stringValue(record, "externalId");
        if (externalId != null && newsItemRepository.existsByExternalIdAndDataSourceId(externalId, dataSourceId)) {
            errors.add(
                new ImportError(record.rowNumber(), "externalId", "NewsItem with this externalId already exists for this DataSource")
            );
        }
    }

    private void validateFileDuplicates(ImportRecord record, Set<String> fileUrls, Set<String> fileExternalIds, List<ImportError> errors) {
        String url = stringValue(record, "url");
        if (url != null) {
            String normalizedUrl = url.toLowerCase(Locale.ROOT);
            if (!fileUrls.add(normalizedUrl)) {
                errors.add(new ImportError(record.rowNumber(), "url", "Duplicate URL in import file"));
            }
        }

        String externalId = stringValue(record, "externalId");
        Long dataSourceId = longValue(record, "dataSourceId");
        if (externalId != null && dataSourceId != null && dataSourceId > 0) {
            String key = dataSourceId + "|" + externalId;
            if (!fileExternalIds.add(key)) {
                errors.add(new ImportError(record.rowNumber(), "externalId", "Duplicate externalId for DataSource in import file"));
            }
        }
    }

    @Override
    protected NewsItemDTO mapRecord(ImportRecord record, Instant batchTimestamp) {
        NewsItemDTO dto = new NewsItemDTO();
        dto.setExternalId(stringValue(record, "externalId"));
        dto.setTitle(stringValue(record, "title"));
        dto.setUrl(stringValue(record, "url"));
        dto.setOriginalText(stringValue(record, "originalText"));
        dto.setPublishedAt(instantValue(record, "publishedAt"));
        dto.setCollectedAt(batchTimestamp);
        dto.setIsDuplicate(false);
        DataSourceDTO dataSource = new DataSourceDTO();
        dataSource.setId(longValue(record, "dataSourceId"));
        dto.setDataSource(dataSource);
        CompetitorDTO competitor = new CompetitorDTO();
        competitor.setId(longValue(record, "competitorId"));
        dto.setCompetitor(competitor);
        dto.setCollectionRun(null);
        return dto;
    }

    @Override
    protected int saveAll(List<NewsItemDTO> records, ImportContext context) {
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            throw new IllegalStateException("Only administrator can import NewsItem");
        }

        return newsItemService.saveImported(records).size();
    }
}
