package com.mycompany.myapp.service.dto;

import java.time.Instant;
import java.util.List;

public class DashboardDTO {

    public long newsCount;
    public long analysisCount;
    public long competitorCount;
    public long rssCount;

    public long positiveCount;
    public long negativeCount;
    public long neutralCount;
    public long unknownCount;

    public List<TopCompetitorDTO> topCompetitors;
    public List<NewsByDayDTO> newsByDay;
    public List<LatestNewsDTO> latestNews;

    public static class TopCompetitorDTO {

        public String competitorName;
        public long newsCount;

        public TopCompetitorDTO(String competitorName, long newsCount) {
            this.competitorName = competitorName;
            this.newsCount = newsCount;
        }
    }

    public static class NewsByDayDTO {

        public String day;
        public long count;

        public NewsByDayDTO(String day, long count) {
            this.day = day;
            this.count = count;
        }
    }

    public static class LatestNewsDTO {

        public Long id;
        public String title;
        public String url;
        public String competitorName;
        public String summary;
        public String sentiment;
        public String topic;
        public String entities;
        public Instant publishedAt;

        public LatestNewsDTO(
            Long id,
            String title,
            String url,
            String competitorName,
            String summary,
            String sentiment,
            String topic,
            String entities,
            Instant publishedAt
        ) {
            this.id = id;
            this.title = title;
            this.url = url;
            this.competitorName = competitorName;
            this.summary = summary;
            this.sentiment = sentiment;
            this.topic = topic;
            this.entities = entities;
            this.publishedAt = publishedAt;
        }
    }
}
