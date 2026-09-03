package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.AnalysisResult;
import com.mycompany.myapp.domain.CiAlert;
import com.mycompany.myapp.domain.NewsItem;
import com.mycompany.myapp.domain.enumeration.Sentiment;
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
import java.util.List;
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

    public DashboardService(
        NewsItemRepository newsItemRepository,
        AnalysisResultRepository analysisResultRepository,
        CompetitorRepository competitorRepository,
        DataSourceRepository dataSourceRepository,
        EntityManager entityManager,
        NewsItemMapper newsItemMapper,
        CiAlertRepository ciAlertRepository
    ) {
        this.newsItemRepository = newsItemRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.competitorRepository = competitorRepository;
        this.dataSourceRepository = dataSourceRepository;
        this.entityManager = entityManager;
        this.newsItemMapper = newsItemMapper;
        this.ciAlertRepository = ciAlertRepository;
    }

    public DashboardDTO getDashboard() {
        DashboardDTO dto = new DashboardDTO();

        dto.newsCount = newsItemRepository.count();
        dto.analysisCount = analysisResultRepository.count();
        dto.competitorCount = competitorRepository.count();
        dto.alertsCount = ciAlertRepository.count();
        dto.highAlertsCount = countHighAlerts();
        dto.sourcesCount = countSources();

        dto.rssCount = dataSourceRepository
            .findAll()
            .stream()
            .filter(source -> source.getSourceType() == SourceType.RSS)
            .count();

        dto.positiveCount = countBySentiment(Sentiment.POSITIVE);
        dto.negativeCount = countBySentiment(Sentiment.NEGATIVE);
        dto.neutralCount = countBySentiment(Sentiment.NEUTRAL);
        dto.unknownCount = countBySentiment(Sentiment.UNKNOWN);

        dto.topCompetitors = loadTopCompetitors();
        dto.newsByDay = loadNewsByDay();
        dto.latestNews = loadLatestNews();
        dto.latestAnalysis = loadLatestAnalysis();
        dto.latestAlerts = loadLatestAlerts();

        return dto;
    }

    @SuppressWarnings("unchecked")
    public List<NewsItemDTO> getLatestFiveNews() {
        long start = System.nanoTime();
        List<NewsItem> latestNews = entityManager
            .createNativeQuery(
                """
                SELECT *
                FROM news_item
                ORDER BY collected_at DESC
                LIMIT 5
                """,
                NewsItem.class
            )
            .getResultList();

        long end = System.nanoTime();

        double timeMs = (end - start) / 1_000_000.0;

        System.out.println("Latest 5 news SQL time: " + timeMs + " ms");

        return latestNews.stream().map(newsItemMapper::toDto).toList();
    }

    public long getNewsCount() {
        Number count = (Number) entityManager
            .createNativeQuery(
                """
                    SELECT COUNT(*)
                    FROM news_item
                """
            )
            .getSingleResult();

        return count.longValue();
    }

    private long countSources() {
        Number count = (Number) entityManager
            .createNativeQuery(
                """
                SELECT COUNT(DISTINCT data_source_id)
                FROM news_item
                WHERE data_source_id IS NOT NULL
                """
            )
            .getSingleResult();
        return count.longValue();
    }

    private long countHighAlerts() {
        Number count = (Number) entityManager
            .createNativeQuery(
                """
                SELECT COUNT(*)
                FROM ci_alert
                WHERE severity IN ('HIGH', 'CRITICAL')
                    """
            )
            .getSingleResult();
        return count.longValue();
    }

    @SuppressWarnings("unchecked")
    private List<DashboardDTO.LatestAnalysisDTO> loadLatestAnalysis() {
        List<AnalysisResult> results = entityManager
            .createNativeQuery(
                """
                SELECT *
                FROM analysis_result
                ORDER BY analyzed_at DESC NULLS LAST, id DESC
                LIMIT 5
                    """,
                AnalysisResult.class
            )
            .getResultList();

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

    @SuppressWarnings("unchecked")
    private List<DashboardDTO.LatestAlertDTO> loadLatestAlerts() {
        List<CiAlert> alerts = entityManager
            .createNativeQuery(
                """
                SELECT *
                FROM ci_alert
                ORDER BY created_at DESC, id DESC
                LIMIT 5
                """,
                CiAlert.class
            )
            .getResultList();

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

    private long countBySentiment(Sentiment sentiment) {
        return entityManager
            .createQuery("select count(a) from AnalysisResult a where a.sentiment = :sentiment", Long.class)
            .setParameter("sentiment", sentiment)
            .getSingleResult();
    }

    private List<DashboardDTO.TopCompetitorDTO> loadTopCompetitors() {
        return entityManager
            .createQuery(
                """
                select new com.mycompany.myapp.service.dto.DashboardDTO$TopCompetitorDTO(
                    c.competitorName,
                    count(n)
                )
                from NewsItem n
                join n.competitor c
                group by c.competitorName
                order by count(n) desc
                """,
                DashboardDTO.TopCompetitorDTO.class
            )
            .setMaxResults(5)
            .getResultList();
    }

    @SuppressWarnings("unchecked")
    private List<DashboardDTO.NewsByDayDTO> loadNewsByDay() {
        List<Object[]> rows = entityManager
            .createNativeQuery(
                """
                SELECT
                    TO_CHAR(published_at, 'DD.MM') AS day,
                    COUNT(*) AS news_count
                FROM news_item
                WHERE published_at IS NOT NULL
                GROUP BY
                    TO_CHAR(published_at, 'DD.MM'),
                    EXTRACT(MONTH FROM published_at),
                    EXTRACT(DAY FROM published_at)
                ORDER BY
                    EXTRACT(MONTH FROM published_at),
                    EXTRACT(DAY FROM published_at)
                """
            )
            .getResultList();

        return rows
            .stream()
            .map(row -> new DashboardDTO.NewsByDayDTO((String) row[0], ((Number) row[1]).longValue()))
            .toList();
    }

    private List<DashboardDTO.LatestNewsDTO> loadLatestNews() {
        List<NewsItem> news = entityManager
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

        return news.stream().map(this::toLatestNewsDTO).toList();
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
