package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.CollectionRun;
import com.mycompany.myapp.domain.DataSource;
import com.mycompany.myapp.domain.NewsItem;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.RunStatus;
import com.mycompany.myapp.domain.enumeration.SourceType;
import com.mycompany.myapp.repository.CollectionRunRepository;
import com.mycompany.myapp.repository.DataSourceRepository;
import com.mycompany.myapp.repository.NewsItemRepository;
import com.mycompany.myapp.repository.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CollectionJobService {

    private static final int ANALYSIS_BATCH_SIZE = 20;
    private final DataSourceRepository dataSourceRepository;
    private final CollectionRunRepository collectionRunRepository;
    private final NewsItemRepository newsItemRepository;
    private final UserRepository userRepository;
    private final RssReaderService rssReaderService;
    private final AnalysisJobService analysisJobService;
    private final NewsItemPersistenceService newsItemPersistenceService;
    private final TenantCacheVersionService tenantCacheVersionService;

    public CollectionJobService(
        DataSourceRepository dataSourceRepository,
        CollectionRunRepository collectionRunRepository,
        NewsItemRepository newsItemRepository,
        UserRepository userRepository,
        RssReaderService rssReaderService,
        AnalysisJobService analysisJobService,
        NewsItemPersistenceService newsItemPersistenceService,
        TenantCacheVersionService tenantCacheVersionService
    ) {
        this.dataSourceRepository = dataSourceRepository;
        this.collectionRunRepository = collectionRunRepository;
        this.newsItemRepository = newsItemRepository;
        this.userRepository = userRepository;
        this.rssReaderService = rssReaderService;
        this.analysisJobService = analysisJobService;
        this.newsItemPersistenceService = newsItemPersistenceService;
        this.tenantCacheVersionService = tenantCacheVersionService;
    }

    public CollectionJobResult runRssCollection(Long userId) {
        User owner = userRepository.findById(userId).orElseThrow(() -> new IllegalStateException("User not found: " + userId));
        CollectionRun run = new CollectionRun();
        run.setStartedAt(Instant.now());
        run.setStatus(RunStatus.RUNNING);
        run.setFoundCount(0);
        run.setProcessedCount(0);
        run.setOwner(owner);
        run = collectionRunRepository.save(run);

        int foundCount = 0;
        int processedCount = 0;
        int duplicateCount = 0;
        int errorCount = 0;
        boolean dataSourceChanged = false;

        List<Long> newsItemIdsForAnalysis = new ArrayList<>();

        StringBuilder errors = new StringBuilder();

        List<DataSource> sources = dataSourceRepository.findAllActiveByCompetitorOwnerIdAndSourceType(userId, SourceType.RSS);

        for (DataSource source : sources) {
            try {
                List<RssReaderService.RssItem> rssItems = rssReaderService.read(source.getUrl());

                foundCount += rssItems.size();
                for (RssReaderService.RssItem rssItem : rssItems) {
                    if (isDuplicate(rssItem, source, userId)) {
                        duplicateCount++;
                        continue;
                    }

                    NewsItem newsItem = new NewsItem();
                    newsItem.setExternalId(truncate(rssItem.guid(), 255));
                    newsItem.setTitle(truncate(rssItem.title(), 500));
                    newsItem.setUrl(truncate(rssItem.link(), 1000));

                    newsItem.setOriginalText(truncate(rssItem.description(), 10000));

                    newsItem.setPublishedAt(rssItem.publishedAt());

                    newsItem.setCollectedAt(Instant.now());

                    newsItem.setIsDuplicate(false);

                    newsItem.setDataSource(source);

                    newsItem.setCompetitor(source.getCompetitor());

                    newsItem.setCollectionRun(run);

                    Long newsItemId = newsItemPersistenceService.save(newsItem);

                    newsItemIdsForAnalysis.add(newsItemId);
                    processedCount++;
                }

                source.setLastCheckedAt(Instant.now());
                dataSourceRepository.save(source);
                dataSourceChanged = true;
            } catch (Exception e) {
                errorCount++;
                errors.append("Источник: ").append(source.getSourceName()).append(" — ").append(e.getMessage()).append("; ");
            }
        }

        run.setFinishedAt(Instant.now());

        run.setFoundCount(foundCount);

        run.setProcessedCount(processedCount);

        if (errorCount > 0) {
            run.setStatus(RunStatus.FAILED);
            run.setErrorMessage(truncate(errors.toString(), 4000));
        } else {
            run.setStatus(RunStatus.SUCCESS);
        }
        collectionRunRepository.save(run);

        if (processedCount > 0 || dataSourceChanged) {
            tenantCacheVersionService.invalidate(userId);
        }

        for (int from = 0; from < newsItemIdsForAnalysis.size(); from += ANALYSIS_BATCH_SIZE) {
            int to = Math.min(from + ANALYSIS_BATCH_SIZE, newsItemIdsForAnalysis.size());
            List<Long> batchIds = List.copyOf(newsItemIdsForAnalysis.subList(from, to));
            System.out.println("Sending TOON analysis batch: " + batchIds.size() + " news, IDs: " + batchIds);
            analysisJobService.analyzeBatchAsync(batchIds, userId);
        }
        return new CollectionJobResult(foundCount, processedCount, duplicateCount, errorCount);
    }

    private boolean isDuplicate(RssReaderService.RssItem rssItem, DataSource source, Long userId) {
        if (
            rssItem.link() != null &&
            !rssItem.link().isBlank() &&
            newsItemRepository.existsByUrlAndCompetitorOwnerId(rssItem.link(), userId)
        ) {
            return true;
        }

        return (
            rssItem.guid() != null &&
            !rssItem.guid().isBlank() &&
            newsItemRepository.existsByExternalIdAndDataSourceId(rssItem.guid(), source.getId())
        );
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    public record CollectionJobResult(int foundCount, int processedCount, int duplicateCount, int errorCount) {}
}
