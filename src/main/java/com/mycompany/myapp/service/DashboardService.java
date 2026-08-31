package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.AnalysisResult;
import com.mycompany.myapp.domain.NewsItem;
import com.mycompany.myapp.domain.enumeration.Sentiment;
import com.mycompany.myapp.domain.enumeration.SourceType;
import com.mycompany.myapp.repository.AnalysisResultRepository;
import com.mycompany.myapp.repository.CompetitorRepository;
import com.mycompany.myapp.repository.DataSourceRepository;
import com.mycompany.myapp.repository.NewsItemRepository;
import com.mycompany.myapp.service.dto.DashboardDTO;
import com.mycompany.myapp.service.dto.NewsItemDTO;
import com.mycompany.myapp.service.mapper.NewsItemMapper;
import jakarta.persistence.EntityManager;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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

    public DashboardService(
        NewsItemRepository newsItemRepository,
        AnalysisResultRepository analysisResultRepository,
        CompetitorRepository competitorRepository,
        DataSourceRepository dataSourceRepository,
        EntityManager entityManager,
        NewsItemMapper newsItemMapper
    ) {
        this.newsItemRepository = newsItemRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.competitorRepository = competitorRepository;
        this.dataSourceRepository = dataSourceRepository;
        this.entityManager = entityManager;
        this.newsItemMapper = newsItemMapper;
    }

    public DashboardDTO getDashboard() {
        DashboardDTO dto = new DashboardDTO();

        dto.newsCount = newsItemRepository.count();
        dto.analysisCount = analysisResultRepository.count();
        dto.competitorCount = competitorRepository.count();

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
                    c.name,
                    count(n)
                )
                from NewsItem n
                join n.competitor c
                group by c.name
                order by count(n) desc
                """,
                DashboardDTO.TopCompetitorDTO.class
            )
            .setMaxResults(5)
            .getResultList();
    }

    private List<DashboardDTO.NewsByDayDTO> loadNewsByDay() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM").withZone(ZoneId.systemDefault());

        List<NewsItem> news = entityManager
            .createQuery(
                """
                select n
                from NewsItem n
                order by n.publishedAt asc
                """,
                NewsItem.class
            )
            .getResultList();

        return news
            .stream()
            .filter(n -> n.getPublishedAt() != null)
            .collect(
                java.util.stream.Collectors.groupingBy(
                    n -> formatter.format(n.getPublishedAt()),
                    java.util.LinkedHashMap::new,
                    java.util.stream.Collectors.counting()
                )
            )
            .entrySet()
            .stream()
            .map(e -> new DashboardDTO.NewsByDayDTO(e.getKey(), e.getValue()))
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
