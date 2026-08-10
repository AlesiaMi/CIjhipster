package com.mycompany.myapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.domain.AnalysisResult;
import com.mycompany.myapp.domain.NewsItem;
import com.mycompany.myapp.domain.enumeration.AnalysisStatus;
import com.mycompany.myapp.domain.enumeration.Sentiment;
import com.mycompany.myapp.repository.AnalysisResultRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiAnalysisService {

    @Value("${openrouter.api-key}")
    private String apiKey;

    @Value("${openrouter.model}")
    private String model;

    private final AnalysisResultRepository analysisResultRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public GeminiAnalysisService(AnalysisResultRepository analysisResultRepository) {
        this.analysisResultRepository = analysisResultRepository;
    }

    public void analyzeNews(NewsItem newsItem) {
        try {
            String prompt = """
                Ты система конкурентной разведки.

                Проанализируй новость.

                Верни JSON следующего вида:

                {
                  "summary":"краткое содержание",
                  "sentiment":"POSITIVE|NEGATIVE|NEUTRAL",
                  "topic":"главная тема",
                  "entities":"через запятую основные компании, люди, продукты",
                  "riskSource":"если есть риск — кратко, иначе пустая строка"
                }

                Никакого дополнительного текста.
                Только JSON.

                Заголовок:

                %s

                Текст:

                %s
                """.formatted(newsItem.getTitle(), newsItem.getOriginalText());

            String requestBody = """
                {
                  "model": %s,
                  "messages": [
                    {
                      "role": "user",
                      "content": %s
                    }
                  ],
                  "temperature": 0.2,
                  "response_format": {
                    "type": "json_object"
                  }
                }
                """.formatted(objectMapper.writeValueAsString(model), objectMapper.writeValueAsString(prompt));

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("========== OPENROUTER RESPONSE ==========");
            System.out.println("HTTP status: " + response.statusCode());
            System.out.println(response.body());
            System.out.println("=========================================");

            JsonNode root = objectMapper.readTree(response.body());

            if (response.statusCode() < 200 || response.statusCode() >= 300 || root.has("error")) {
                AnalysisResult result = new AnalysisResult();

                result.setNewsItem(newsItem);
                result.setStatus(AnalysisStatus.FAILED);
                result.setSentiment(Sentiment.UNKNOWN);
                result.setAnalyzedAt(Instant.now());

                String errorMessage = root.path("error").path("message").asText();

                if (errorMessage == null || errorMessage.isBlank()) {
                    errorMessage = "OpenRouter HTTP error " + response.statusCode();
                }

                result.setErrorMessage(errorMessage);

                analysisResultRepository.save(result);
                return;
            }

            JsonNode message = root.path("choices").path(0).path("message");

            String json = message.path("content").asText();

            if (json == null || json.isBlank()) {
                throw new IllegalStateException("OpenRouter returned empty content");
            }

            JsonNode ai = objectMapper.readTree(json);

            AnalysisResult result = new AnalysisResult();

            result.setNewsItem(newsItem);

            result.setSummary(ai.path("summary").asText());

            result.setTopic(ai.path("topic").asText());

            result.setEntities(ai.path("entities").asText());

            result.setRiskSource(ai.path("riskSource").asText());

            try {
                result.setSentiment(Sentiment.valueOf(ai.path("sentiment").asText("NEUTRAL").toUpperCase()));
            } catch (Exception ex) {
                result.setSentiment(Sentiment.NEUTRAL);
            }

            result.setStatus(AnalysisStatus.SUCCESS);

            result.setModelName(model);

            result.setAnalyzedAt(Instant.now());

            analysisResultRepository.save(result);
        } catch (Exception ex) {
            ex.printStackTrace();

            AnalysisResult result = new AnalysisResult();

            result.setNewsItem(newsItem);

            result.setStatus(AnalysisStatus.FAILED);

            result.setSentiment(Sentiment.UNKNOWN);

            result.setAnalyzedAt(Instant.now());

            result.setErrorMessage(ex.getMessage());

            analysisResultRepository.save(result);
        }
    }
}
