package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.AnalysisResult;
import com.mycompany.myapp.domain.CiAlert;
import com.mycompany.myapp.domain.NewsItem;
import com.mycompany.myapp.domain.enumeration.Sentiment;
import com.mycompany.myapp.domain.enumeration.Severity;
import com.mycompany.myapp.domain.enumeration.SourceType;
import com.mycompany.myapp.repository.AnalysisResultRepository;
import com.mycompany.myapp.repository.CiAlertRepository;
import com.mycompany.myapp.repository.CompetitorRepository;
import com.mycompany.myapp.repository.DataSourceRepository;
import com.mycompany.myapp.repository.NewsItemRepository;
import com.mycompany.myapp.service.dto.DashboardDTO;
import com.mycompany.myapp.service.dto.NewsItemDTO;
import com.mycompany.myapp.service.mapper.NewsItemMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final NewsItemRepository newsItemRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final CompetitorRepository competitorRepository;
    private final DataSourceRepository dataSourceRepository;
    private final EntityManager entityManager;
    private final NewsItemMapper newsItemMapper;
    private final CiAlertRepository ciAlertRepository;
    private final CacheManager cacheManager;
    private final TenantCacheVersionService tenantCacheVersionService;

    public DashboardService(
        NewsItemRepository newsItemRepository,
        AnalysisResultRepository analysisResultRepository,
        CompetitorRepository competitorRepository,
        DataSourceRepository dataSourceRepository,
        EntityManager entityManager,
        NewsItemMapper newsItemMapper,
        CiAlertRepository ciAlertRepository,
        CacheManager cacheManager,
        TenantCacheVersionService tenantCacheVersionService
    ) {
        this.newsItemRepository = newsItemRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.competitorRepository = competitorRepository;
        this.dataSourceRepository = dataSourceRepository;
        this.entityManager = entityManager;
        this.newsItemMapper = newsItemMapper;
        this.ciAlertRepository = ciAlertRepository;
        this.cacheManager = cacheManager;
        this.tenantCacheVersionService = tenantCacheVersionService;
    }

    public DashboardDTO getDashboard(Long ownerId) {
        String cacheKey = tenantCacheVersionService.namespace(ownerId) + "|dashboard";

        Cache cache = cacheManager.getCache("dashboards");

        if (cache != null) {
            DashboardDTO cached = cache.get(cacheKey, DashboardDTO.class);

            if (cached != null) {
                return cached;
            }
        }

        DashboardDTO dto = new DashboardDTO();

        dto.newsCount = countNews(ownerId);
        dto.analysisCount = countAnalysis(ownerId);
        dto.competitorCount = countCompetitors(ownerId);

        dto.sourcesCount = countSources(ownerId);
        dto.rssCount = countRssSources(ownerId);

        dto.alertsCount = countAlerts(ownerId);
        dto.highAlertsCount = countHighAlerts(ownerId);

        dto.positiveCount = countBySentiment(Sentiment.POSITIVE, ownerId);
        dto.negativeCount = countBySentiment(Sentiment.NEGATIVE, ownerId);
        dto.neutralCount = countBySentiment(Sentiment.NEUTRAL, ownerId);
        dto.unknownCount = countBySentiment(Sentiment.UNKNOWN, ownerId);

        dto.topCompetitors = loadTopCompetitors(ownerId);
        dto.newsByDay = loadNewsByDay(ownerId);
        dto.latestNews = loadLatestNews(ownerId);
        dto.latestAnalysis = loadLatestAnalysis(ownerId);
        dto.latestAlerts = loadLatestAlerts(ownerId);

        if (cache != null) {
            cache.put(cacheKey, dto);
        }
        return dto;
    }

    /**
     * Endpoint /api/dashboard/latest-news
     */
    public List<NewsItemDTO> getLatestFiveNews(Long ownerId) {
        List<NewsItem> latestNews;

        if (ownerId == null) {
            latestNews = entityManager
                .createQuery(
                    """
                    select n
                    from NewsItem n
                    left join fetch n.dataSource
                    left join fetch n.competitor
                    order by n.collectedAt desc
                    """,
                    NewsItem.class
                )
                .setMaxResults(5)
                .getResultList();
        } else {
            latestNews = entityManager
                .createQuery(
                    """
                    select n
                    from NewsItem n
                    left join fetch n.dataSource
                    join fetch n.competitor c
                    where c.owner.id = :ownerId
                    order by n.collectedAt desc
                    """,
                    NewsItem.class
                )
                .setParameter("ownerId", ownerId)
                .setMaxResults(5)
                .getResultList();
        }

        return latestNews.stream().map(newsItemMapper::toDto).toList();
    }

    /**
     * Endpoint /api/dashboard/news-count
     */
    public long getNewsCount(Long ownerId) {
        return countNews(ownerId);
    }

    private long countNews(Long ownerId) {
        if (ownerId == null) {
            return newsItemRepository.count();
        }

        return entityManager
            .createQuery(
                """
                select count(n)
                from NewsItem n
                join n.competitor c
                where c.owner.id = :ownerId
                """,
                Long.class
            )
            .setParameter("ownerId", ownerId)
            .getSingleResult();
    }

    private long countAnalysis(Long ownerId) {
        if (ownerId == null) {
            return analysisResultRepository.count();
        }

        return entityManager
            .createQuery(
                """
                select count(a)
                from AnalysisResult a
                join a.newsItem n
                join n.competitor c
                where c.owner.id = :ownerId
                """,
                Long.class
            )
            .setParameter("ownerId", ownerId)
            .getSingleResult();
    }

    private long countCompetitors(Long ownerId) {
        if (ownerId == null) {
            return competitorRepository.count();
        }

        return entityManager
            .createQuery(
                """
                select count(c)
                from Competitor c
                where c.owner.id = :ownerId
                """,
                Long.class
            )
            .setParameter("ownerId", ownerId)
            .getSingleResult();
    }

    /**
     * Теперь sourcesCount = количество всех DataSource пользователя,
     * а не только источников, у которых уже успела появиться хотя бы одна новость.
     */
    private long countSources(Long ownerId) {
        if (ownerId == null) {
            return dataSourceRepository.count();
        }

        return entityManager
            .createQuery(
                """
                select count(ds)
                from DataSource ds
                join ds.competitor c
                where c.owner.id = :ownerId
                """,
                Long.class
            )
            .setParameter("ownerId", ownerId)
            .getSingleResult();
    }

    private long countRssSources(Long ownerId) {
        if (ownerId == null) {
            return entityManager
                .createQuery(
                    """
                    select count(ds)
                    from DataSource ds
                    where ds.sourceType = :sourceType
                    """,
                    Long.class
                )
                .setParameter("sourceType", SourceType.RSS)
                .getSingleResult();
        }

        return entityManager
            .createQuery(
                """
                select count(ds)
                from DataSource ds
                join ds.competitor c
                where c.owner.id = :ownerId
                  and ds.sourceType = :sourceType
                """,
                Long.class
            )
            .setParameter("ownerId", ownerId)
            .setParameter("sourceType", SourceType.RSS)
            .getSingleResult();
    }

    /**
     * Alert принадлежит пользователю через:
     * CiAlert -> AnalysisResult -> NewsItem -> Competitor -> owner
     */
    private long countAlerts(Long ownerId) {
        if (ownerId == null) {
            return ciAlertRepository.count();
        }

        return entityManager
            .createQuery(
                """
                select count(alert)
                from CiAlert alert
                join alert.analysisResult analysis
                join analysis.newsItem news
                join news.competitor competitor
                where competitor.owner.id = :ownerId
                """,
                Long.class
            )
            .setParameter("ownerId", ownerId)
            .getSingleResult();
    }

    private long countHighAlerts(Long ownerId) {
        if (ownerId == null) {
            return entityManager
                .createQuery(
                    """
                    select count(alert)
                    from CiAlert alert
                    where alert.severity = :high
                       or alert.severity = :critical
                    """,
                    Long.class
                )
                .setParameter("high", Severity.HIGH)
                .setParameter("critical", Severity.CRITICAL)
                .getSingleResult();
        }

        return entityManager
            .createQuery(
                """
                select count(alert)
                from CiAlert alert
                join alert.analysisResult analysis
                join analysis.newsItem news
                join news.competitor competitor
                where competitor.owner.id = :ownerId
                  and (
                       alert.severity = :high
                       or alert.severity = :critical
                  )
                """,
                Long.class
            )
            .setParameter("ownerId", ownerId)
            .setParameter("high", Severity.HIGH)
            .setParameter("critical", Severity.CRITICAL)
            .getSingleResult();
    }

    private long countBySentiment(Sentiment sentiment, Long ownerId) {
        if (ownerId == null) {
            return entityManager
                .createQuery(
                    """
                    select count(a)
                    from AnalysisResult a
                    where a.sentiment = :sentiment
                    """,
                    Long.class
                )
                .setParameter("sentiment", sentiment)
                .getSingleResult();
        }

        return entityManager
            .createQuery(
                """
                select count(a)
                from AnalysisResult a
                join a.newsItem n
                join n.competitor c
                where c.owner.id = :ownerId
                  and a.sentiment = :sentiment
                """,
                Long.class
            )
            .setParameter("ownerId", ownerId)
            .setParameter("sentiment", sentiment)
            .getSingleResult();
    }

    private List<DashboardDTO.TopCompetitorDTO> loadTopCompetitors(Long ownerId) {
        if (ownerId == null) {
            return entityManager
                .createQuery(
                    """
                    select new com.mycompany.myapp.service.dto.DashboardDTO$TopCompetitorDTO(
                        c.competitorName,
                        count(n)
                    )
                    from NewsItem n
                    join n.competitor c
                    group by c.id, c.competitorName
                    order by count(n) desc
                    """,
                    DashboardDTO.TopCompetitorDTO.class
                )
                .setMaxResults(5)
                .getResultList();
        }

        return entityManager
            .createQuery(
                """
                select new com.mycompany.myapp.service.dto.DashboardDTO$TopCompetitorDTO(
                    c.competitorName,
                    count(n)
                )
                from NewsItem n
                join n.competitor c
                where c.owner.id = :ownerId
                group by c.id, c.competitorName
                order by count(n) desc
                """,
                DashboardDTO.TopCompetitorDTO.class
            )
            .setParameter("ownerId", ownerId)
            .setMaxResults(5)
            .getResultList();
    }

    @SuppressWarnings("unchecked")
    private List<DashboardDTO.NewsByDayDTO> loadNewsByDay(Long ownerId) {
        Query query;

        if (ownerId == null) {
            query = entityManager.createNativeQuery(
                """
                SELECT
                    TO_CHAR(DATE(n.published_at), 'DD.MM') AS day,
                    COUNT(*) AS news_count
                FROM news_item n
                WHERE n.published_at IS NOT NULL
                GROUP BY DATE(n.published_at)
                ORDER BY DATE(n.published_at)
                """
            );
        } else {
            query = entityManager.createNativeQuery(
                """
                SELECT
                    TO_CHAR(DATE(n.published_at), 'DD.MM') AS day,
                    COUNT(*) AS news_count
                FROM news_item n
                JOIN competitor c
                  ON c.id = n.competitor_id
                WHERE n.published_at IS NOT NULL
                  AND c.owner_id = :ownerId
                GROUP BY DATE(n.published_at)
                ORDER BY DATE(n.published_at)
                """
            );

            query.setParameter("ownerId", ownerId);
        }

        List<Object[]> rows = query.getResultList();

        return rows
            .stream()
            .map(row -> new DashboardDTO.NewsByDayDTO((String) row[0], ((Number) row[1]).longValue()))
            .toList();
    }

    private List<DashboardDTO.LatestNewsDTO> loadLatestNews(Long ownerId) {
        List<NewsItem> news;

        if (ownerId == null) {
            news = entityManager
                .createQuery(
                    """
                    select n
                    from NewsItem n
                    left join fetch n.competitor
                    left join fetch n.analysisResult
                    order by n.collectedAt desc
                    """,
                    NewsItem.class
                )
                .setMaxResults(10)
                .getResultList();
        } else {
            news = entityManager
                .createQuery(
                    """
                    select n
                    from NewsItem n
                    join fetch n.competitor c
                    left join fetch n.analysisResult
                    where c.owner.id = :ownerId
                    order by n.collectedAt desc
                    """,
                    NewsItem.class
                )
                .setParameter("ownerId", ownerId)
                .setMaxResults(10)
                .getResultList();
        }

        return news.stream().map(this::toLatestNewsDTO).toList();
    }

    private List<DashboardDTO.LatestAnalysisDTO> loadLatestAnalysis(Long ownerId) {
        List<AnalysisResult> results;

        if (ownerId == null) {
            results = entityManager
                .createQuery(
                    """
                    select analysis
                    from AnalysisResult analysis
                    order by analysis.analyzedAt desc nulls last,
                             analysis.id desc
                    """,
                    AnalysisResult.class
                )
                .setMaxResults(5)
                .getResultList();
        } else {
            results = entityManager
                .createQuery(
                    """
                    select analysis
                    from AnalysisResult analysis
                    join analysis.newsItem news
                    join news.competitor competitor
                    where competitor.owner.id = :ownerId
                    order by analysis.analyzedAt desc nulls last,
                             analysis.id desc
                    """,
                    AnalysisResult.class
                )
                .setParameter("ownerId", ownerId)
                .setMaxResults(5)
                .getResultList();
        }

        return results
            .stream()
            .map(analysis ->
                new DashboardDTO.LatestAnalysisDTO(
                    analysis.getId(),
                    analysis.getTopic(),
                    analysis.getSummary(),
                    analysis.getSentiment() != null ? analysis.getSentiment().name() : "",
                    analysis.getModelName()
                )
            )
            .toList();
    }

    private List<DashboardDTO.LatestAlertDTO> loadLatestAlerts(Long ownerId) {
        List<CiAlert> alerts;

        if (ownerId == null) {
            alerts = entityManager
                .createQuery(
                    """
                    select alert
                    from CiAlert alert
                    order by alert.createdAt desc,
                             alert.id desc
                    """,
                    CiAlert.class
                )
                .setMaxResults(5)
                .getResultList();
        } else {
            alerts = entityManager
                .createQuery(
                    """
                    select alert
                    from CiAlert alert
                    join alert.analysisResult analysis
                    join analysis.newsItem news
                    join news.competitor competitor
                    where competitor.owner.id = :ownerId
                    order by alert.createdAt desc,
                             alert.id desc
                    """,
                    CiAlert.class
                )
                .setParameter("ownerId", ownerId)
                .setMaxResults(5)
                .getResultList();
        }

        return alerts
            .stream()
            .map(alert ->
                new DashboardDTO.LatestAlertDTO(
                    alert.getId(),
                    alert.getTitle(),
                    alert.getMessage(),
                    alert.getSeverity() != null ? alert.getSeverity().name() : "",
                    alert.getStatus() != null ? alert.getStatus().name() : ""
                )
            )
            .toList();
    }

    private DashboardDTO.LatestNewsDTO toLatestNewsDTO(NewsItem newsItem) {
        AnalysisResult analysis = newsItem.getAnalysisResult();

        return new DashboardDTO.LatestNewsDTO(
            newsItem.getId(),
            newsItem.getTitle(),
            newsItem.getUrl(),
            newsItem.getCompetitor() != null ? newsItem.getCompetitor().getCompetitorName() : "",
            analysis != null ? analysis.getSummary() : "",
            analysis != null && analysis.getSentiment() != null ? analysis.getSentiment().name() : "",
            analysis != null ? analysis.getTopic() : "",
            analysis != null ? analysis.getEntities() : "",
            newsItem.getPublishedAt()
        );
    }
}
