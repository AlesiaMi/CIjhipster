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

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
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

                Верни JSON следующего вида.

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
                  "contents": [
                    {
                      "parts": [
                        {
                          "text": %s
                        }
                      ]
                    }
                  ],
                  "generationConfig": {
                    "temperature": 0.2,
                    "responseMimeType": "application/json"
                  }
                }
                """.formatted(objectMapper.writeValueAsString(prompt));

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("========== GEMINI RESPONSE ==========");
            System.out.println(response.body());
            System.out.println("====================================");

            JsonNode root = objectMapper.readTree(response.body());

            if (root.has("error")) {
                AnalysisResult result = new AnalysisResult();

                result.setNewsItem(newsItem);
                result.setStatus(AnalysisStatus.FAILED);
                result.setSentiment(Sentiment.UNKNOWN);
                result.setAnalyzedAt(Instant.now());
                result.setErrorMessage(root.path("error").path("message").asText());

                analysisResultRepository.save(result);
                return;
            }

            JsonNode candidate = root.path("candidates").get(0);

            JsonNode part = candidate.path("content").path("parts").get(0);

            String json = part.path("text").asText();

            JsonNode ai = objectMapper.readTree(json);

            AnalysisResult result = new AnalysisResult();

            result.setNewsItem(newsItem);

            result.setSummary(ai.path("summary").asText());

            result.setTopic(ai.path("topic").asText());

            result.setEntities(ai.path("entities").asText());

            result.setRiskSource(ai.path("riskSource").asText());

            try {
                result.setSentiment(Sentiment.valueOf(ai.path("sentiment").asText("NEUTRAL")));
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
