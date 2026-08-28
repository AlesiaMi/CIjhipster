package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.CollectionRun;
import com.mycompany.myapp.domain.DataSource;
import com.mycompany.myapp.domain.NewsItem;
import com.mycompany.myapp.domain.enumeration.RunStatus;
import com.mycompany.myapp.domain.enumeration.SourceType;
import com.mycompany.myapp.repository.CollectionRunRepository;
import com.mycompany.myapp.repository.DataSourceRepository;
import com.mycompany.myapp.repository.NewsItemRepository;
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
    private final RssReaderService rssReaderService;
    private final AnalysisJobService analysisJobService;
    private final NewsItemPersistenceService newsItemPersistenceService;

    public CollectionJobService(
        DataSourceRepository dataSourceRepository,
        CollectionRunRepository collectionRunRepository,
        NewsItemRepository newsItemRepository,
        RssReaderService rssReaderService,
        AnalysisJobService analysisJobService,
        NewsItemPersistenceService newsItemPersistenceService
    ) {
        this.dataSourceRepository = dataSourceRepository;
        this.collectionRunRepository = collectionRunRepository;
        this.newsItemRepository = newsItemRepository;
        this.rssReaderService = rssReaderService;
        this.analysisJobService = analysisJobService;
        this.newsItemPersistenceService = newsItemPersistenceService;
    }

    public CollectionJobResult runRssCollection() {
        CollectionRun run = new CollectionRun();
        run.setStartedAt(Instant.now());
        run.setStatus(RunStatus.RUNNING);
        run.setFoundCount(0);
        run.setProcessedCount(0);
        run = collectionRunRepository.save(run);

        int foundCount = 0;
        int processedCount = 0;
        int duplicateCount = 0;
        int errorCount = 0;

        List<Long> newsItemIdsForAnalysis = new ArrayList<>();

        StringBuilder errors = new StringBuilder();

        List<DataSource> sources = dataSourceRepository
            .findAll()
            .stream()
            .filter(source -> Boolean.TRUE.equals(source.getIsActive()))
            .filter(source -> source.getSourceType() == SourceType.RSS)
            .toList();

        for (DataSource source : sources) {
            try {
                List<RssReaderService.RssItem> rssItems = rssReaderService.read(source.getUrl());

                foundCount += rssItems.size();
                for (RssReaderService.RssItem rssItem : rssItems) {
                    if (isDuplicate(rssItem)) {
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

        for (int from = 0; from < newsItemIdsForAnalysis.size(); from += ANALYSIS_BATCH_SIZE) {
            int to = Math.min(from + ANALYSIS_BATCH_SIZE, newsItemIdsForAnalysis.size());
            List<Long> batchIds = List.copyOf(newsItemIdsForAnalysis.subList(from, to));
            System.out.println("Sending TOON analysis batch: " + batchIds.size() + " news, IDs: " + batchIds);
            analysisJobService.analyzeBatchAsync(batchIds);
        }
        return new CollectionJobResult(foundCount, processedCount, duplicateCount, errorCount);
    }

    private boolean isDuplicate(RssReaderService.RssItem rssItem) {
        if (rssItem.link() != null && newsItemRepository.existsByUrl(rssItem.link())) {
            return true;
        }

        return (rssItem.guid() != null && !rssItem.guid().isBlank() && newsItemRepository.existsByExternalId(rssItem.guid()));
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    public record CollectionJobResult(int foundCount, int processedCount, int duplicateCount, int errorCount) {}
}
