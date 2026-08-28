package com.mycompany.myapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.domain.AnalysisResult;
import com.mycompany.myapp.domain.NewsItem;
import com.mycompany.myapp.domain.enumeration.AnalysisStatus;
import com.mycompany.myapp.domain.enumeration.Sentiment;
import com.mycompany.myapp.repository.AnalysisResultRepository;
import com.mycompany.myapp.repository.NewsItemRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiAnalysisService {

    private static final int MAX_BATCH_SIZE = 20;

    private static final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";

    @Value("${openrouter.api-key}")
    private String apiKey;

    @Value("${openrouter.model}")
    private String model;

    private final AnalysisResultRepository analysisResultRepository;

    private final NewsItemRepository newsItemRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();

    public GeminiAnalysisService(AnalysisResultRepository analysisResultRepository, NewsItemRepository newsItemRepository) {
        this.analysisResultRepository = analysisResultRepository;
        this.newsItemRepository = newsItemRepository;
    }

    public void analyzeNewsBatch(List<Long> newsItemIds) {
        if (newsItemIds == null || newsItemIds.isEmpty()) {
            return;
        }

        List<NewsItem> newsItems = newsItemIds
            .stream()
            .limit(MAX_BATCH_SIZE)
            .map(id -> newsItemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("NewsItem not found: " + id)))
            .toList();

        System.out.println();
        System.out.println("========== TOON BATCH START ==========");
        System.out.println("News count: " + newsItems.size());
        System.out.println("News IDs: " + newsItems.stream().map(NewsItem::getId).toList());
        System.out.println("======================================");

        analyzeToonBatch(newsItems);
    }

    private void analyzeToonBatch(List<NewsItem> newsItems) {
        try {
            String prompt = buildToonBatchPrompt(newsItems);
            JsonNode root = sendToOpenRouter(prompt);
            JsonNode choice = root.path("choices").path(0);
            String finishReason = choice.path("finish_reason").asText("");
            if ("content_filter".equalsIgnoreCase(finishReason)) {
                throw new IllegalStateException("OpenRouter content filter");
            }

            JsonNode contentNode = choice.path("message").path("content");

            if (!contentNode.isTextual()) {
                throw new IllegalStateException("OpenRouter returned empty batch content");
            }

            String json = contentNode.asText();

            if (json == null || json.isBlank()) {
                throw new IllegalStateException("OpenRouter returned empty batch content");
            }

            JsonNode ai = objectMapper.readTree(json);
            JsonNode results = ai.path("news");

            if (!results.isArray()) {
                throw new IllegalStateException("OpenRouter batch response " + "does not contain news array");
            }

            for (JsonNode item : results) {
                long newsItemId = item.path("id").asLong(-1);
                NewsItem newsItem = newsItems
                    .stream()
                    .filter(n -> n.getId() != null && n.getId() == newsItemId)
                    .findFirst()
                    .orElse(null);
                if (newsItem == null) {
                    System.err.println("Unknown NewsItem ID " + "returned by AI: " + newsItemId);
                    continue;
                }
                saveSuccessfulResult(newsItem, item);
            }

            System.out.println("TOON batch analysis finished");
        } catch (Exception ex) {
            ex.printStackTrace();
            for (NewsItem newsItem : newsItems) {
                saveFailedResult(newsItem, ex.getMessage());
            }
        }
    }

    private void saveSuccessfulResult(NewsItem newsItem, JsonNode item) {
        AnalysisResult result = new AnalysisResult();
        result.setNewsItem(newsItem);
        result.setSummary(item.path("summary").asText());
        result.setTopic(item.path("topic").asText());
        result.setEntities(item.path("entities").asText());
        result.setRiskSource(item.path("riskSource").asText());
        try {
            result.setSentiment(Sentiment.valueOf(item.path("sentiment").asText("NEUTRAL").toUpperCase()));
        } catch (Exception ex) {
            result.setSentiment(Sentiment.NEUTRAL);
        }
        result.setStatus(AnalysisStatus.SUCCESS);
        result.setModelName(model);
        result.setAnalyzedAt(Instant.now());
        analysisResultRepository.save(result);
    }

    private String buildToonBatchPrompt(List<NewsItem> newsItems) {
        StringBuilder toon = new StringBuilder();
        toon.append("news[").append(newsItems.size()).append("]{id,title,text}:\n");
        for (NewsItem newsItem : newsItems) {
            toon.append(newsItem.getId())
                .append(",")
                .append(toToonString(newsItem.getTitle()))
                .append(",")
                .append(toToonString(newsItem.getOriginalText()))
                .append("\n");
        }
        return """
        Ты система конкурентной разведки.

        Проанализируй все переданные новости.

        Передано новостей: %d.

        Для КАЖДОЙ новости обязательно верни отдельный результат.

        В массиве news должно быть ровно %d элементов.

        Верни JSON-объект строго следующего вида:

        {
          "news": [
            {
              "id": 0,
              "summary": "краткое содержание",
              "sentiment": "POSITIVE|NEGATIVE|NEUTRAL",
              "topic": "главная тема",
              "entities": "через запятую основные компании, люди, продукты",
              "riskSource": "если есть риск — кратко, иначе пустая строка"
            }
          ]
        }

        Сохрани id каждой исходной новости.

        Не пропускай новости.

        Никакого дополнительного текста.
        Только JSON.

        Новости в формате TOON:

        %s
        """.formatted(newsItems.size(), newsItems.size(), toon.toString());
    }

    private JsonNode sendToOpenRouter(String prompt) throws Exception {
        String requestBody = buildRequestBody(prompt);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(OPENROUTER_URL))
            .timeout(Duration.ofSeconds(120))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonNode root = objectMapper.readTree(response.body());
        if (response.statusCode() < 200 || response.statusCode() >= 300 || root.has("error")) {
            String errorMessage = root.path("error").path("message").asText("");
            if (errorMessage.isBlank()) {
                errorMessage = "OpenRouter HTTP error " + response.statusCode();
            }

            throw new IllegalStateException(errorMessage);
        }
        return root;
    }

    private String buildRequestBody(String prompt) throws Exception {
        return """
        {
          "model": %s,
          "messages": [
            {
              "role": "user",
              "content": %s
            }
          ],
          "temperature": 0,
          "response_format": {
            "type": "json_object"
          }
        }
        """.formatted(objectMapper.writeValueAsString(model), objectMapper.writeValueAsString(prompt));
    }

    private String toToonString(String value) {
        try {
            return objectMapper.writeValueAsString(value == null ? "" : value);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot encode TOON value", ex);
        }
    }

    private void saveFailedResult(NewsItem newsItem, String errorMessage) {
        AnalysisResult result = new AnalysisResult();
        result.setNewsItem(newsItem);
        result.setStatus(AnalysisStatus.FAILED);
        result.setSentiment(Sentiment.UNKNOWN);
        result.setAnalyzedAt(Instant.now());
        result.setModelName(model);
        result.setErrorMessage(errorMessage);
        analysisResultRepository.save(result);
    }
}
