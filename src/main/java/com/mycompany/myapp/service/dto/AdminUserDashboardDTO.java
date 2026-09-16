package com.mycompany.myapp.service.dto;

import java.time.Instant;
import java.util.List;

public class AdminUserDashboardDTO {

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
    public long collectionRunsCount;

    public List<CompetitorItemDTO> competitors;
    public List<SourceItemDTO> sources;
    public List<RunItemDTO> recentRuns;

    public static class CompetitorItemDTO {

        public Long id;
        public String name;

        public CompetitorItemDTO(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class SourceItemDTO {

        public Long id;
        public String name;
        public String url;
        public String sourceType;
        public boolean active;
        public Long competitorId;
        public String competitorName;

        public SourceItemDTO(
            Long id,
            String name,
            String url,
            String sourceType,
            boolean active,
            Long competitorId,
            String competitorName
        ) {
            this.id = id;
            this.name = name;
            this.url = url;
            this.sourceType = sourceType;
            this.active = active;
            this.competitorId = competitorId;
            this.competitorName = competitorName;
        }
    }

    public static class RunItemDTO {

        public Long id;
        public String status;
        public Instant startedAt;
        public Instant finishedAt;
        public int foundCount;
        public int processedCount;
        public String errorMessage;

        public RunItemDTO(
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
