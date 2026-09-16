package com.mycompany.myapp.service.dto;

import java.time.Instant;
import java.util.List;

public class AdminDashboardDTO {

    public long usersCount;
    public long competitorsCount;
    public long sourcesCount;
    public long rssCount;
    public long newsCount;
    public long analysisCount;
    public long collectionRunsCount;

    public List<UserSummaryDTO> users;

    public static class UserSummaryDTO {

        public Long id;
        public String login;
        public String email;
        public boolean activated;
        public Instant createdDate;

        public long competitorsCount;
        public long sourcesCount;
        public long rssCount;
        public long newsCount;
        public long analysisCount;

        public LastRunDTO lastRun;

        public UserSummaryDTO(
            Long id,
            String login,
            String email,
            boolean activated,
            Instant createdDate,
            long competitorsCount,
            long sourcesCount,
            long rssCount,
            long newsCount,
            long analysisCount,
            LastRunDTO lastRun
        ) {
            this.id = id;
            this.login = login;
            this.email = email;
            this.activated = activated;
            this.createdDate = createdDate;
            this.competitorsCount = competitorsCount;
            this.sourcesCount = sourcesCount;
            this.rssCount = rssCount;
            this.newsCount = newsCount;
            this.analysisCount = analysisCount;
            this.lastRun = lastRun;
        }
    }

    public static class LastRunDTO {

        public Long id;
        public String status;
        public Instant startedAt;
        public Instant finishedAt;
        public int foundCount;
        public int processedCount;
        public String errorMessage;

        public LastRunDTO(
            Long id,
            String status,
            Instant startedAt,
            Instant finishedAt,
            int foundCount,
            int processedCount,
            String errorMessage
        ) {
            this.id = id;
            this.status = status;
            this.startedAt = startedAt;
            this.finishedAt = finishedAt;
            this.foundCount = foundCount;
            this.processedCount = processedCount;
            this.errorMessage = errorMessage;
        }
    }
}
