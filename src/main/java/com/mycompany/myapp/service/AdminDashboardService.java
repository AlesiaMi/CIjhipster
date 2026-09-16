package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.CollectionRun;
import com.mycompany.myapp.domain.Competitor;
import com.mycompany.myapp.domain.DataSource;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.SourceType;
import com.mycompany.myapp.repository.AnalysisResultRepository;
import com.mycompany.myapp.repository.CollectionRunRepository;
import com.mycompany.myapp.repository.CompetitorRepository;
import com.mycompany.myapp.repository.DataSourceRepository;
import com.mycompany.myapp.repository.NewsItemRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.service.dto.AdminDashboardDTO;
import com.mycompany.myapp.service.dto.AdminUserDashboardDTO;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final CompetitorRepository competitorRepository;
    private final DataSourceRepository dataSourceRepository;
    private final NewsItemRepository newsItemRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final CollectionRunRepository collectionRunRepository;
    private final EntityManager entityManager;

    public AdminDashboardService(
        UserRepository userRepository,
        CompetitorRepository competitorRepository,
        DataSourceRepository dataSourceRepository,
        NewsItemRepository newsItemRepository,
        AnalysisResultRepository analysisResultRepository,
        CollectionRunRepository collectionRunRepository,
        EntityManager entityManager
    ) {
        this.userRepository = userRepository;
        this.competitorRepository = competitorRepository;
        this.dataSourceRepository = dataSourceRepository;
        this.newsItemRepository = newsItemRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.collectionRunRepository = collectionRunRepository;
        this.entityManager = entityManager;
    }

    public AdminDashboardDTO getDashboard() {
        AdminDashboardDTO dto = new AdminDashboardDTO();

        dto.usersCount = countBusinessUsers();
        dto.competitorsCount = competitorRepository.count();
        dto.sourcesCount = dataSourceRepository.count();
        dto.rssCount = countAllRssSources();
        dto.newsCount = newsItemRepository.count();
        dto.analysisCount = analysisResultRepository.count();
        dto.collectionRunsCount = collectionRunRepository.count();

        dto.users = getUsers();

        return dto;
    }

    public List<AdminDashboardDTO.UserSummaryDTO> getUsers() {
        return userRepository.findAll().stream().filter(this::isBusinessUser).map(this::toUserSummary).toList();
    }

    public AdminUserDashboardDTO getUserDashboard(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        AdminUserDashboardDTO dto = new AdminUserDashboardDTO();

        dto.id = user.getId();
        dto.login = user.getLogin();
        dto.email = user.getEmail();
        dto.activated = user.isActivated();
        dto.createdDate = user.getCreatedDate();

        dto.competitorsCount = countCompetitors(userId);
        dto.sourcesCount = countSources(userId);
        dto.rssCount = countRssSources(userId);
        dto.newsCount = countNews(userId);
        dto.analysisCount = countAnalysis(userId);
        dto.collectionRunsCount = countRuns(userId);

        dto.competitors = loadCompetitors(userId);
        dto.sources = loadSources(userId);
        dto.recentRuns = loadRecentRuns(userId);

        return dto;
    }

    private AdminDashboardDTO.UserSummaryDTO toUserSummary(User user) {
        Long userId = user.getId();

        return new AdminDashboardDTO.UserSummaryDTO(
            userId,
            user.getLogin(),
            user.getEmail(),
            user.isActivated(),
            user.getCreatedDate(),
            countCompetitors(userId),
            countSources(userId),
            countRssSources(userId),
            countNews(userId),
            countAnalysis(userId),
            loadLastRun(userId)
        );
    }

    private boolean isBusinessUser(User user) {
        return (
            !"admin".equalsIgnoreCase(user.getLogin()) &&
            !"system".equalsIgnoreCase(user.getLogin()) &&
            !"anonymoususer".equalsIgnoreCase(user.getLogin())
        );
    }

    private long countBusinessUsers() {
        return userRepository.findAll().stream().filter(this::isBusinessUser).count();
    }

    private long countAllRssSources() {
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

    private long countCompetitors(Long userId) {
        return entityManager
            .createQuery(
                """
                select count(c)
                from Competitor c
                where c.owner.id = :userId
                """,
                Long.class
            )
            .setParameter("userId", userId)
            .getSingleResult();
    }

    private long countSources(Long userId) {
        return entityManager
            .createQuery(
                """
                select count(ds)
                from DataSource ds
                join ds.competitor c
                where c.owner.id = :userId
                """,
                Long.class
            )
            .setParameter("userId", userId)
            .getSingleResult();
    }

    private long countRssSources(Long userId) {
        return entityManager
            .createQuery(
                """
                select count(ds)
                from DataSource ds
                join ds.competitor c
                where c.owner.id = :userId
                  and ds.sourceType = :sourceType
                """,
                Long.class
            )
            .setParameter("userId", userId)
            .setParameter("sourceType", SourceType.RSS)
            .getSingleResult();
    }

    private long countNews(Long userId) {
        return entityManager
            .createQuery(
                """
                select count(n)
                from NewsItem n
                join n.competitor c
                where c.owner.id = :userId
                """,
                Long.class
            )
            .setParameter("userId", userId)
            .getSingleResult();
    }

    private long countAnalysis(Long userId) {
        return entityManager
            .createQuery(
                """
                select count(a)
                from AnalysisResult a
                join a.newsItem n
                join n.competitor c
                where c.owner.id = :userId
                """,
                Long.class
            )
            .setParameter("userId", userId)
            .getSingleResult();
    }

    private long countRuns(Long userId) {
        return entityManager
            .createQuery(
                """
                select count(r)
                from CollectionRun r
                where r.owner.id = :userId
                """,
                Long.class
            )
            .setParameter("userId", userId)
            .getSingleResult();
    }

    private List<AdminUserDashboardDTO.CompetitorItemDTO> loadCompetitors(Long userId) {
        List<Competitor> competitors = entityManager
            .createQuery(
                """
                select c
                from Competitor c
                where c.owner.id = :userId
                order by c.competitorName
                """,
                Competitor.class
            )
            .setParameter("userId", userId)
            .getResultList();

        return competitors
            .stream()
            .map(c -> new AdminUserDashboardDTO.CompetitorItemDTO(c.getId(), c.getCompetitorName()))
            .toList();
    }

    private List<AdminUserDashboardDTO.SourceItemDTO> loadSources(Long userId) {
        List<DataSource> sources = entityManager
            .createQuery(
                """
                select ds
                from DataSource ds
                join fetch ds.competitor c
                where c.owner.id = :userId
                order by ds.id desc
                """,
                DataSource.class
            )
            .setParameter("userId", userId)
            .getResultList();

        return sources
            .stream()
            .map(ds ->
                new AdminUserDashboardDTO.SourceItemDTO(
                    ds.getId(),
                    ds.getSourceName(),
                    ds.getUrl(),
                    ds.getSourceType() != null ? ds.getSourceType().name() : null,
                    Boolean.TRUE.equals(ds.getIsActive()),
                    ds.getCompetitor() != null ? ds.getCompetitor().getId() : null,
                    ds.getCompetitor() != null ? ds.getCompetitor().getCompetitorName() : null
                )
            )
            .toList();
    }

    private AdminDashboardDTO.LastRunDTO loadLastRun(Long userId) {
        List<CollectionRun> runs = entityManager
            .createQuery(
                """
                select r
                from CollectionRun r
                where r.owner.id = :userId
                order by r.startedAt desc
                """,
                CollectionRun.class
            )
            .setParameter("userId", userId)
            .setMaxResults(1)
            .getResultList();

        if (runs.isEmpty()) {
            return null;
        }

        CollectionRun run = runs.getFirst();

        return new AdminDashboardDTO.LastRunDTO(
            run.getId(),
            run.getStatus() != null ? run.getStatus().name() : null,
            run.getStartedAt(),
            run.getFinishedAt(),
            run.getFoundCount() != null ? run.getFoundCount() : 0,
            run.getProcessedCount() != null ? run.getProcessedCount() : 0,
            run.getErrorMessage()
        );
    }

    private List<AdminUserDashboardDTO.RunItemDTO> loadRecentRuns(Long userId) {
        List<CollectionRun> runs = entityManager
            .createQuery(
                """
                select r
                from CollectionRun r
                where r.owner.id = :userId
                order by r.startedAt desc
                """,
                CollectionRun.class
            )
            .setParameter("userId", userId)
            .setMaxResults(10)
            .getResultList();

        return runs
            .stream()
            .map(run ->
                new AdminUserDashboardDTO.RunItemDTO(
                    run.getId(),
                    run.getStatus() != null ? run.getStatus().name() : null,
                    run.getStartedAt(),
                    run.getFinishedAt(),
                    run.getFoundCount() != null ? run.getFoundCount() : 0,
                    run.getProcessedCount() != null ? run.getProcessedCount() : 0,
                    run.getErrorMessage()
                )
            )
            .toList();
    }
}
