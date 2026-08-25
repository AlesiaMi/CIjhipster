/*package com.mycompany.myapp.service;

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

    private final NewsItemRepository newsItemRepository;

    public GeminiAnalysisService(AnalysisResultRepository analysisResultRepository, NewsItemRepository newsItemRepository) {
        this.analysisResultRepository = analysisResultRepository;
        this.newsItemRepository = newsItemRepository;
    }

    public void analyzeNewsById(Long newsItemId) {
        NewsItem newsItem = newsItemRepository
            .findById(newsItemId)
            .orElseThrow(() -> new IllegalArgumentException("NewsItem not found: " + newsItemId));

        analyzeNews(newsItem);
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
*/

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
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiAnalysisService {

    @Value("${openrouter.api-key}")
    private String apiKey;

    @Value("${openrouter.model}")
    private String model;

    private final AnalysisResultRepository analysisResultRepository;
    private final NewsItemRepository newsItemRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public GeminiAnalysisService(AnalysisResultRepository analysisResultRepository, NewsItemRepository newsItemRepository) {
        this.analysisResultRepository = analysisResultRepository;
        this.newsItemRepository = newsItemRepository;
    }

    public void analyzeNewsById(Long newsItemId) {
        NewsItem newsItem = newsItemRepository
            .findById(newsItemId)
            .orElseThrow(() -> new IllegalArgumentException("NewsItem not found: " + newsItemId));

        analyzeNews(newsItem);
    }

    public void analyzeNews(NewsItem newsItem) {
        try {
            String prompt = buildJsonPrompt(newsItem);

            JsonNode root = sendToOpenRouter(prompt);

            if (root == null) {
                saveFailedResult(newsItem, "Empty OpenRouter response");
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

            saveFailedResult(newsItem, ex.getMessage());
        }
    }

    /**
     * Тест JSON vs TOON.
     *
     * Берёт одну и ту же NewsItem и дважды отправляет её
     * в одну и ту же модель:
     *
     * 1. JSON-oriented prompt
     * 2. TOON-oriented prompt
     *
     * НИЧЕГО не сохраняет в AnalysisResult.
     *
     * Результат сравнения выводится только в терминал.
     */
    public void compareFormats(Long newsItemId) {
        try {
            NewsItem newsItem = newsItemRepository
                .findById(newsItemId)
                .orElseThrow(() -> new IllegalArgumentException("NewsItem not found: " + newsItemId));

            System.out.println();
            System.out.println("==============================================");
            System.out.println("JSON vs TOON TEST");
            System.out.println("NewsItem ID: " + newsItemId);
            System.out.println("Model: " + model);
            System.out.println("Title: " + newsItem.getTitle());
            System.out.println("==============================================");

            String jsonPrompt = buildJsonPrompt(newsItem);

            System.out.println();
            System.out.println("---------- JSON TEST ----------");

            TokenUsage jsonUsage = sendForTokenTest(jsonPrompt);

            /*
             * Небольшая пауза, чтобы два тестовых запроса
             * не улетали в OpenRouter практически одновременно.
             */
            Thread.sleep(2000);

            String toonPrompt = buildToonPrompt(newsItem);

            System.out.println();
            System.out.println("---------- TOON TEST ----------");

            TokenUsage toonUsage = sendForTokenTest(toonPrompt);

            int savedPromptTokens = jsonUsage.promptTokens() - toonUsage.promptTokens();

            double savedPercent = 0.0;

            if (jsonUsage.promptTokens() > 0) {
                savedPercent = ((double) savedPromptTokens / jsonUsage.promptTokens()) * 100.0;
            }

            System.out.println();
            System.out.println("==============================================");
            System.out.println("JSON vs TOON RESULT");
            System.out.println("==============================================");

            System.out.println("JSON PROMPT TOKENS: " + jsonUsage.promptTokens());

            System.out.println("TOON PROMPT TOKENS: " + toonUsage.promptTokens());

            System.out.println("SAVED PROMPT TOKENS: " + savedPromptTokens);

            System.out.printf("PROMPT SAVING: %.2f%%%n", savedPercent);

            System.out.println();

            System.out.println("JSON COMPLETION TOKENS: " + jsonUsage.completionTokens());

            System.out.println("TOON COMPLETION TOKENS: " + toonUsage.completionTokens());

            System.out.println();

            System.out.println("JSON TOTAL TOKENS: " + jsonUsage.totalTokens());

            System.out.println("TOON TOTAL TOKENS: " + toonUsage.totalTokens());

            System.out.println("==============================================");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void compareFormatsBatch(List<Long> newsItemIds) {
        try {
            int jsonPromptTotal = 0;
            int toonPromptTotal = 0;

            int jsonCompletionTotal = 0;
            int toonCompletionTotal = 0;

            int jsonTotal = 0;
            int toonTotal = 0;

            int processed = 0;

            System.out.println();
            System.out.println("==============================================");
            System.out.println("JSON vs TOON BATCH TEST");
            System.out.println("Model: " + model);
            System.out.println("Requested news count: " + newsItemIds.size());
            System.out.println("==============================================");

            for (Long newsItemId : newsItemIds) {
                NewsItem newsItem = newsItemRepository
                    .findById(newsItemId)
                    .orElseThrow(() -> new IllegalArgumentException("NewsItem not found: " + newsItemId));

                System.out.println();
                System.out.println("----------------------------------------------");
                System.out.println("NewsItem ID: " + newsItemId);
                System.out.println("Title: " + newsItem.getTitle());
                System.out.println("----------------------------------------------");

                // JSON
                String jsonPrompt = buildJsonPrompt(newsItem);

                System.out.println("JSON TEST:");

                TokenUsage jsonUsage = sendForTokenTest(jsonPrompt);

                Thread.sleep(2000);

                // TOON
                String toonPrompt = buildToonPrompt(newsItem);

                System.out.println("TOON TEST:");

                TokenUsage toonUsage = sendForTokenTest(toonPrompt);

                // Суммируем JSON
                jsonPromptTotal += jsonUsage.promptTokens();
                jsonCompletionTotal += jsonUsage.completionTokens();
                jsonTotal += jsonUsage.totalTokens();

                // Суммируем TOON
                toonPromptTotal += toonUsage.promptTokens();
                toonCompletionTotal += toonUsage.completionTokens();
                toonTotal += toonUsage.totalTokens();

                processed++;

                Thread.sleep(2000);
            }

            int savedPromptTokens = jsonPromptTotal - toonPromptTotal;

            double savedPromptPercent = 0.0;

            if (jsonPromptTotal > 0) {
                savedPromptPercent = ((double) savedPromptTokens / jsonPromptTotal) * 100.0;
            }

            double avgJsonPrompt = processed > 0 ? (double) jsonPromptTotal / processed : 0.0;

            double avgToonPrompt = processed > 0 ? (double) toonPromptTotal / processed : 0.0;

            double avgJsonTotal = processed > 0 ? (double) jsonTotal / processed : 0.0;

            double avgToonTotal = processed > 0 ? (double) toonTotal / processed : 0.0;

            System.out.println();
            System.out.println("==============================================");
            System.out.println("JSON vs TOON BATCH RESULT");
            System.out.println("==============================================");

            System.out.println("NEWS PROCESSED: " + processed);

            System.out.println();

            System.out.println("JSON PROMPT TOKENS TOTAL: " + jsonPromptTotal);

            System.out.println("TOON PROMPT TOKENS TOTAL: " + toonPromptTotal);

            System.out.println("SAVED PROMPT TOKENS: " + savedPromptTokens);

            System.out.printf("PROMPT SAVING: %.2f%%%n", savedPromptPercent);

            System.out.println();

            System.out.printf("AVG JSON PROMPT TOKENS: %.2f%n", avgJsonPrompt);

            System.out.printf("AVG TOON PROMPT TOKENS: %.2f%n", avgToonPrompt);

            System.out.println();

            System.out.println("JSON COMPLETION TOKENS TOTAL: " + jsonCompletionTotal);

            System.out.println("TOON COMPLETION TOKENS TOTAL: " + toonCompletionTotal);

            System.out.println();

            System.out.println("JSON TOTAL TOKENS: " + jsonTotal);

            System.out.println("TOON TOTAL TOKENS: " + toonTotal);

            System.out.println();

            System.out.printf("AVG JSON TOTAL TOKENS: %.2f%n", avgJsonTotal);

            System.out.printf("AVG TOON TOTAL TOKENS: %.2f%n", avgToonTotal);

            System.out.println("==============================================");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void compareFormatsSingleBatch(List<Long> newsItemIds) {
        try {
            List<NewsItem> newsItems = newsItemIds
                .stream()
                .map(id -> newsItemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("NewsItem not found: " + id)))
                .toList();

            System.out.println();
            System.out.println("==============================================");
            System.out.println("JSON vs TOON SINGLE BATCH TEST");
            System.out.println("Model: " + model);
            System.out.println("News count: " + newsItems.size());
            System.out.println("==============================================");

            /*
             * Один prompt содержит ВСЕ новости в JSON.
             */
            String jsonPrompt = buildJsonBatchPrompt(newsItems);

            System.out.println();
            System.out.println("---------- JSON BATCH TEST ----------");

            TokenUsage jsonUsage = sendForTokenTest(jsonPrompt);

            Thread.sleep(2000);

            /*
             * Один prompt содержит те же самые новости в TOON.
             */
            String toonPrompt = buildToonBatchPrompt(newsItems);

            System.out.println();
            System.out.println("---------- TOON BATCH TEST ----------");

            TokenUsage toonUsage = sendForTokenTest(toonPrompt);

            int savedPromptTokens = jsonUsage.promptTokens() - toonUsage.promptTokens();

            int savedPromptBytes = jsonUsage.promptBytes() - toonUsage.promptBytes();

            double savedPromptBytesPercent = 0.0;

            if (jsonUsage.promptBytes() > 0) {
                savedPromptBytesPercent = ((double) savedPromptBytes / jsonUsage.promptBytes()) * 100.0;
            }

            int savedRequestBytes = jsonUsage.requestBytes() - toonUsage.requestBytes();

            double savedRequestBytesPercent = 0.0;

            if (jsonUsage.requestBytes() > 0) {
                savedRequestBytesPercent = ((double) savedRequestBytes / jsonUsage.requestBytes()) * 100.0;
            }
            double savedPromptPercent = 0.0;

            if (jsonUsage.promptTokens() > 0) {
                savedPromptPercent = ((double) savedPromptTokens / jsonUsage.promptTokens()) * 100.0;
            }

            int totalDifference = jsonUsage.totalTokens() - toonUsage.totalTokens();

            double totalSavingPercent = 0.0;

            if (jsonUsage.totalTokens() > 0) {
                totalSavingPercent = ((double) totalDifference / jsonUsage.totalTokens()) * 100.0;
            }

            System.out.println();
            System.out.println("==============================================");
            System.out.println("JSON vs TOON SINGLE BATCH RESULT");
            System.out.println("==============================================");

            System.out.println("NEWS COUNT: " + newsItems.size());

            System.out.println();

            System.out.println("JSON PROMPT TOKENS: " + jsonUsage.promptTokens());

            System.out.println("TOON PROMPT TOKENS: " + toonUsage.promptTokens());

            System.out.println("SAVED PROMPT TOKENS: " + savedPromptTokens);

            System.out.printf("PROMPT SAVING: %.2f%%%n", savedPromptPercent);

            System.out.println();

            System.out.println("JSON COMPLETION TOKENS: " + jsonUsage.completionTokens());

            System.out.println("TOON COMPLETION TOKENS: " + toonUsage.completionTokens());

            System.out.println();

            System.out.println("JSON TOTAL TOKENS: " + jsonUsage.totalTokens());

            System.out.println("TOON TOTAL TOKENS: " + toonUsage.totalTokens());

            System.out.println("SAVED TOTAL TOKENS: " + totalDifference);

            System.out.printf("TOTAL SAVING: %.2f%%%n", totalSavingPercent);

            System.out.println();

            System.out.println("JSON PROMPT BYTES: " + jsonUsage.promptBytes());

            System.out.println("TOON PROMPT BYTES: " + toonUsage.promptBytes());

            System.out.println("SAVED PROMPT BYTES: " + savedPromptBytes);

            System.out.printf("PROMPT BYTE SAVING: %.2f%%%n", savedPromptBytesPercent);

            System.out.println();

            System.out.println("JSON REQUEST BYTES: " + jsonUsage.requestBytes());

            System.out.println("TOON REQUEST BYTES: " + toonUsage.requestBytes());

            System.out.println("SAVED REQUEST BYTES: " + savedRequestBytes);

            System.out.printf("REQUEST BYTE SAVING: %.2f%%%n", savedRequestBytesPercent);

            System.out.println("==============================================");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void compareFormatsDetailedBatch(List<Long> newsItemIds) {
        try {
            long jsonPromptTokensTotal = 0;
            long toonPromptTokensTotal = 0;

            long jsonCompletionTokensTotal = 0;
            long toonCompletionTokensTotal = 0;

            long jsonTotalTokensTotal = 0;
            long toonTotalTokensTotal = 0;

            long jsonPromptBytesTotal = 0;
            long toonPromptBytesTotal = 0;

            long jsonAnswerBytesTotal = 0;
            long toonAnswerBytesTotal = 0;

            long jsonPromptAndAnswerBytesTotal = 0;
            long toonPromptAndAnswerBytesTotal = 0;

            long jsonRequestBytesTotal = 0;
            long toonRequestBytesTotal = 0;

            long jsonRequestAndAnswerBytesTotal = 0;
            long toonRequestAndAnswerBytesTotal = 0;

            int processed = 0;

            System.out.println();
            System.out.println("============================================================");
            System.out.println("JSON vs TOON DETAILED SEPARATE REQUESTS TEST");
            System.out.println("Model: " + model);
            System.out.println("News count: " + newsItemIds.size());
            System.out.println("Each news item is analyzed separately");
            System.out.println("============================================================");

            for (Long newsItemId : newsItemIds) {
                NewsItem newsItem = newsItemRepository
                    .findById(newsItemId)
                    .orElseThrow(() -> new IllegalArgumentException("NewsItem not found: " + newsItemId));

                System.out.println();
                System.out.println("============================================================");
                System.out.println("NEWS ITEM #" + (processed + 1));
                System.out.println("ID: " + newsItemId);
                System.out.println("TITLE: " + newsItem.getTitle());
                System.out.println("============================================================");

                // ============================================================
                // JSON
                // ============================================================

                String jsonPrompt = buildJsonPrompt(newsItem);

                System.out.println();
                System.out.println("--------------- JSON ---------------");

                TokenUsage jsonUsage = sendForTokenTest(jsonPrompt);

                Thread.sleep(2000);

                // ============================================================
                // TOON
                // ============================================================

                String toonPrompt = buildToonPrompt(newsItem);

                System.out.println();
                System.out.println("--------------- TOON ---------------");

                TokenUsage toonUsage = sendForTokenTest(toonPrompt);

                // ============================================================
                // РАЗНИЦА ПО ЭТОЙ НОВОСТИ
                // ============================================================

                int savedPromptTokens = jsonUsage.promptTokens() - toonUsage.promptTokens();

                int savedCompletionTokens = jsonUsage.completionTokens() - toonUsage.completionTokens();

                int savedTotalTokens = jsonUsage.totalTokens() - toonUsage.totalTokens();

                int savedPromptBytes = jsonUsage.promptBytes() - toonUsage.promptBytes();

                int savedAnswerBytes = jsonUsage.answerBytes() - toonUsage.answerBytes();

                int savedPromptAndAnswerBytes = jsonUsage.promptAndAnswerBytes() - toonUsage.promptAndAnswerBytes();

                int savedRequestBytes = jsonUsage.requestBytes() - toonUsage.requestBytes();

                int savedRequestAndAnswerBytes = jsonUsage.requestAndAnswerBytes() - toonUsage.requestAndAnswerBytes();

                double promptTokenSavingPercent = percentage(savedPromptTokens, jsonUsage.promptTokens());

                double totalTokenSavingPercent = percentage(savedTotalTokens, jsonUsage.totalTokens());

                double promptByteSavingPercent = percentage(savedPromptBytes, jsonUsage.promptBytes());

                double answerByteSavingPercent = percentage(savedAnswerBytes, jsonUsage.answerBytes());

                double promptAndAnswerByteSavingPercent = percentage(savedPromptAndAnswerBytes, jsonUsage.promptAndAnswerBytes());

                double requestByteSavingPercent = percentage(savedRequestBytes, jsonUsage.requestBytes());

                double requestAndAnswerByteSavingPercent = percentage(savedRequestAndAnswerBytes, jsonUsage.requestAndAnswerBytes());

                System.out.println();
                System.out.println("--------------- RESULT FOR NEWS " + newsItemId + " ---------------");

                System.out.println();
                System.out.println("TOKENS:");

                System.out.println("JSON prompt tokens: " + jsonUsage.promptTokens());

                System.out.println("TOON prompt tokens: " + toonUsage.promptTokens());

                System.out.println("Saved prompt tokens: " + savedPromptTokens);

                System.out.printf("Prompt token saving: %.2f%%%n", promptTokenSavingPercent);

                System.out.println();

                System.out.println("JSON completion tokens: " + jsonUsage.completionTokens());

                System.out.println("TOON completion tokens: " + toonUsage.completionTokens());

                System.out.println("Saved completion tokens: " + savedCompletionTokens);

                System.out.println();

                System.out.println("JSON total tokens: " + jsonUsage.totalTokens());

                System.out.println("TOON total tokens: " + toonUsage.totalTokens());

                System.out.println("Saved total tokens: " + savedTotalTokens);

                System.out.printf("Total token saving: %.2f%%%n", totalTokenSavingPercent);

                System.out.println();
                System.out.println("BYTES:");

                System.out.println("JSON prompt bytes: " + jsonUsage.promptBytes());

                System.out.println("TOON prompt bytes: " + toonUsage.promptBytes());

                System.out.println("Saved prompt bytes: " + savedPromptBytes);

                System.out.printf("Prompt byte saving: %.2f%%%n", promptByteSavingPercent);

                System.out.println();

                System.out.println("JSON answer bytes: " + jsonUsage.answerBytes());

                System.out.println("TOON answer bytes: " + toonUsage.answerBytes());

                System.out.println("Saved answer bytes: " + savedAnswerBytes);

                System.out.printf("Answer byte saving: %.2f%%%n", answerByteSavingPercent);

                System.out.println();

                System.out.println("JSON prompt + answer bytes: " + jsonUsage.promptAndAnswerBytes());

                System.out.println("TOON prompt + answer bytes: " + toonUsage.promptAndAnswerBytes());

                System.out.println("Saved prompt + answer bytes: " + savedPromptAndAnswerBytes);

                System.out.printf("Prompt + answer byte saving: %.2f%%%n", promptAndAnswerByteSavingPercent);

                System.out.println();

                System.out.println("JSON request bytes: " + jsonUsage.requestBytes());

                System.out.println("TOON request bytes: " + toonUsage.requestBytes());

                System.out.println("Saved request bytes: " + savedRequestBytes);

                System.out.printf("Request byte saving: %.2f%%%n", requestByteSavingPercent);

                System.out.println();

                System.out.println("JSON request + answer bytes: " + jsonUsage.requestAndAnswerBytes());

                System.out.println("TOON request + answer bytes: " + toonUsage.requestAndAnswerBytes());

                System.out.println("Saved request + answer bytes: " + savedRequestAndAnswerBytes);

                System.out.printf("Request + answer byte saving: %.2f%%%n", requestAndAnswerByteSavingPercent);

                // НАКАПЛИВАЕМ ОБЩУЮ СТАТИСТИКУ

                jsonPromptTokensTotal += jsonUsage.promptTokens();

                toonPromptTokensTotal += toonUsage.promptTokens();

                jsonCompletionTokensTotal += jsonUsage.completionTokens();

                toonCompletionTokensTotal += toonUsage.completionTokens();

                jsonTotalTokensTotal += jsonUsage.totalTokens();

                toonTotalTokensTotal += toonUsage.totalTokens();

                jsonPromptBytesTotal += jsonUsage.promptBytes();

                toonPromptBytesTotal += toonUsage.promptBytes();

                jsonAnswerBytesTotal += jsonUsage.answerBytes();

                toonAnswerBytesTotal += toonUsage.answerBytes();

                jsonPromptAndAnswerBytesTotal += jsonUsage.promptAndAnswerBytes();

                toonPromptAndAnswerBytesTotal += toonUsage.promptAndAnswerBytes();

                jsonRequestBytesTotal += jsonUsage.requestBytes();

                toonRequestBytesTotal += toonUsage.requestBytes();

                jsonRequestAndAnswerBytesTotal += jsonUsage.requestAndAnswerBytes();

                toonRequestAndAnswerBytesTotal += toonUsage.requestAndAnswerBytes();

                processed++;

                Thread.sleep(2000);
            }

            // ОБЩАЯ СТАТИСТИКА

            long savedPromptTokensTotal = jsonPromptTokensTotal - toonPromptTokensTotal;

            long savedCompletionTokensTotal = jsonCompletionTokensTotal - toonCompletionTokensTotal;

            long savedTotalTokensTotal = jsonTotalTokensTotal - toonTotalTokensTotal;

            long savedPromptBytesTotal = jsonPromptBytesTotal - toonPromptBytesTotal;

            long savedAnswerBytesTotal = jsonAnswerBytesTotal - toonAnswerBytesTotal;

            long savedPromptAndAnswerBytesTotal = jsonPromptAndAnswerBytesTotal - toonPromptAndAnswerBytesTotal;

            long savedRequestBytesTotal = jsonRequestBytesTotal - toonRequestBytesTotal;

            long savedRequestAndAnswerBytesTotal = jsonRequestAndAnswerBytesTotal - toonRequestAndAnswerBytesTotal;

            System.out.println();
            System.out.println("============================================================");
            System.out.println("JSON vs TOON FINAL DETAILED RESULT");
            System.out.println("============================================================");

            System.out.println("NEWS PROCESSED: " + processed);

            System.out.println("AI REQUESTS TOTAL: " + (processed * 2));

            // TOKENS TOTAL

            System.out.println();
            System.out.println("=============== TOKENS TOTAL ===============");

            System.out.println("JSON PROMPT TOKENS TOTAL: " + jsonPromptTokensTotal);

            System.out.println("TOON PROMPT TOKENS TOTAL: " + toonPromptTokensTotal);

            System.out.println("SAVED PROMPT TOKENS TOTAL: " + savedPromptTokensTotal);

            System.out.printf("PROMPT TOKEN SAVING TOTAL: %.2f%%%n", percentage(savedPromptTokensTotal, jsonPromptTokensTotal));

            System.out.println();

            System.out.println("JSON COMPLETION TOKENS TOTAL: " + jsonCompletionTokensTotal);

            System.out.println("TOON COMPLETION TOKENS TOTAL: " + toonCompletionTokensTotal);

            System.out.println("SAVED COMPLETION TOKENS TOTAL: " + savedCompletionTokensTotal);

            System.out.printf("COMPLETION TOKEN SAVING TOTAL: %.2f%%%n", percentage(savedCompletionTokensTotal, jsonCompletionTokensTotal));

            System.out.println();

            System.out.println("JSON ALL TOKENS TOTAL: " + jsonTotalTokensTotal);

            System.out.println("TOON ALL TOKENS TOTAL: " + toonTotalTokensTotal);

            System.out.println("SAVED ALL TOKENS TOTAL: " + savedTotalTokensTotal);

            System.out.printf("ALL TOKEN SAVING TOTAL: %.2f%%%n", percentage(savedTotalTokensTotal, jsonTotalTokensTotal));

            // ================================================================
            // TOKENS AVERAGE
            // ================================================================

            System.out.println();
            System.out.println("=============== TOKENS AVERAGE ===============");

            System.out.printf("AVG JSON PROMPT TOKENS PER REQUEST: %.2f%n", average(jsonPromptTokensTotal, processed));

            System.out.printf("AVG TOON PROMPT TOKENS PER REQUEST: %.2f%n", average(toonPromptTokensTotal, processed));

            System.out.printf("AVG JSON COMPLETION TOKENS PER REQUEST: %.2f%n", average(jsonCompletionTokensTotal, processed));

            System.out.printf("AVG TOON COMPLETION TOKENS PER REQUEST: %.2f%n", average(toonCompletionTokensTotal, processed));

            System.out.printf("AVG JSON TOTAL TOKENS PER REQUEST: %.2f%n", average(jsonTotalTokensTotal, processed));

            System.out.printf("AVG TOON TOTAL TOKENS PER REQUEST: %.2f%n", average(toonTotalTokensTotal, processed));

            // ================================================================
            // BYTES TOTAL
            // ================================================================

            System.out.println();
            System.out.println("=============== BYTES TOTAL ===============");

            System.out.println("JSON PROMPT BYTES TOTAL: " + jsonPromptBytesTotal);

            System.out.println("TOON PROMPT BYTES TOTAL: " + toonPromptBytesTotal);

            System.out.println("SAVED PROMPT BYTES TOTAL: " + savedPromptBytesTotal);

            System.out.printf("PROMPT BYTE SAVING TOTAL: %.2f%%%n", percentage(savedPromptBytesTotal, jsonPromptBytesTotal));

            System.out.println();

            System.out.println("JSON ANSWER BYTES TOTAL: " + jsonAnswerBytesTotal);

            System.out.println("TOON ANSWER BYTES TOTAL: " + toonAnswerBytesTotal);

            System.out.println("SAVED ANSWER BYTES TOTAL: " + savedAnswerBytesTotal);

            System.out.printf("ANSWER BYTE SAVING TOTAL: %.2f%%%n", percentage(savedAnswerBytesTotal, jsonAnswerBytesTotal));

            System.out.println();

            System.out.println("JSON PROMPT + ANSWER BYTES TOTAL: " + jsonPromptAndAnswerBytesTotal);

            System.out.println("TOON PROMPT + ANSWER BYTES TOTAL: " + toonPromptAndAnswerBytesTotal);

            System.out.println("SAVED PROMPT + ANSWER BYTES TOTAL: " + savedPromptAndAnswerBytesTotal);

            System.out.printf(
                "PROMPT + ANSWER BYTE SAVING TOTAL: %.2f%%%n",
                percentage(savedPromptAndAnswerBytesTotal, jsonPromptAndAnswerBytesTotal)
            );

            System.out.println();

            System.out.println("JSON REQUEST BYTES TOTAL: " + jsonRequestBytesTotal);

            System.out.println("TOON REQUEST BYTES TOTAL: " + toonRequestBytesTotal);

            System.out.println("SAVED REQUEST BYTES TOTAL: " + savedRequestBytesTotal);

            System.out.printf("REQUEST BYTE SAVING TOTAL: %.2f%%%n", percentage(savedRequestBytesTotal, jsonRequestBytesTotal));

            System.out.println();

            System.out.println("JSON REQUEST + ANSWER BYTES TOTAL: " + jsonRequestAndAnswerBytesTotal);

            System.out.println("TOON REQUEST + ANSWER BYTES TOTAL: " + toonRequestAndAnswerBytesTotal);

            System.out.println("SAVED REQUEST + ANSWER BYTES TOTAL: " + savedRequestAndAnswerBytesTotal);

            System.out.printf(
                "REQUEST + ANSWER BYTE SAVING TOTAL: %.2f%%%n",
                percentage(savedRequestAndAnswerBytesTotal, jsonRequestAndAnswerBytesTotal)
            );

            // ================================================================
            // BYTES AVERAGE
            // ================================================================

            System.out.println();
            System.out.println("=============== BYTES AVERAGE ===============");

            System.out.printf("AVG JSON PROMPT BYTES: %.2f%n", average(jsonPromptBytesTotal, processed));

            System.out.printf("AVG TOON PROMPT BYTES: %.2f%n", average(toonPromptBytesTotal, processed));

            System.out.printf("AVG JSON ANSWER BYTES: %.2f%n", average(jsonAnswerBytesTotal, processed));

            System.out.printf("AVG TOON ANSWER BYTES: %.2f%n", average(toonAnswerBytesTotal, processed));

            System.out.printf("AVG JSON PROMPT + ANSWER BYTES: %.2f%n", average(jsonPromptAndAnswerBytesTotal, processed));

            System.out.printf("AVG TOON PROMPT + ANSWER BYTES: %.2f%n", average(toonPromptAndAnswerBytesTotal, processed));

            System.out.printf("AVG JSON REQUEST + ANSWER BYTES: %.2f%n", average(jsonRequestAndAnswerBytesTotal, processed));

            System.out.printf("AVG TOON REQUEST + ANSWER BYTES: %.2f%n", average(toonRequestAndAnswerBytesTotal, processed));

            System.out.println("============================================================");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void analyzeNewsBatch(List<Long> newsItemIds) {
        if (newsItemIds == null || newsItemIds.isEmpty()) {
            return;
        }

        List<NewsItem> newsItems = newsItemIds
            .stream()
            .map(id -> newsItemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("NewsItem not found: " + id)))
            .toList();

        int batchSize = 20;

        for (int from = 0; from < newsItems.size(); from += batchSize) {
            int to = Math.min(from + batchSize, newsItems.size());

            List<NewsItem> batch = newsItems.subList(from, to);

            analyzeToonBatch(batch);
        }
    }

    private void analyzeToonBatch(List<NewsItem> newsItems) {
        try {
            String prompt = buildToonBatchPrompt(newsItems);

            System.out.println();
            System.out.println("========== TOON BATCH PROMPT ==========");
            System.out.println(prompt);
            System.out.println("=======================================");
            System.out.println();

            JsonNode root = sendToOpenRouter(prompt);

            String json = root.path("choices").path(0).path("message").path("content").asText();

            if (json == null || json.isBlank()) {
                throw new IllegalStateException("OpenRouter returned empty batch content");
            }

            JsonNode ai = objectMapper.readTree(json);

            JsonNode results = ai.path("news");

            if (!results.isArray()) {
                throw new IllegalStateException("OpenRouter batch response does not contain news array");
            }

            for (JsonNode item : results) {
                long newsItemId = item.path("id").asLong(-1);

                NewsItem newsItem = newsItems
                    .stream()
                    .filter(n -> n.getId() != null && n.getId() == newsItemId)
                    .findFirst()
                    .orElse(null);

                if (newsItem == null) {
                    System.err.println("Unknown NewsItem ID returned by AI: " + newsItemId);

                    continue;
                }

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
        } catch (Exception ex) {
            ex.printStackTrace();

            /*
             * Если весь batch упал —
             * помечаем каждую его новость ошибкой.
             */
            for (NewsItem newsItem : newsItems) {
                saveFailedResult(newsItem, ex.getMessage());
            }
        }
    }

    private String buildJsonBatchPrompt(List<NewsItem> newsItems) {
        try {
            var newsArray = objectMapper.createArrayNode();

            for (NewsItem newsItem : newsItems) {
                var news = objectMapper.createObjectNode();

                news.put("id", newsItem.getId());
                news.put("title", newsItem.getTitle() == null ? "" : newsItem.getTitle());
                news.put("text", newsItem.getOriginalText() == null ? "" : newsItem.getOriginalText());

                newsArray.add(news);
            }

            String jsonNews = objectMapper.writeValueAsString(newsArray);

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

            Новости в формате JSON:

            %s
            """.formatted(newsItems.size(), newsItems.size(), jsonNews);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot build JSON batch prompt", ex);
        }
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

    private String buildJsonPrompt(NewsItem newsItem) {
        return """
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

        Новость в формате JSON:

        {
          "title": %s,
          "text": %s
        }
        """.formatted(toJsonString(newsItem.getTitle()), toJsonString(newsItem.getOriginalText()));
    }

    private String buildToonPrompt(NewsItem newsItem) {
        return """
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

        Новость в формате TOON:

        title: %s
        text: %s
        """.formatted(toToonString(newsItem.getTitle()), toToonString(newsItem.getOriginalText()));
    }

    private TokenUsage sendForTokenTest(String prompt) throws Exception {
        String requestBody = buildRequestBody(prompt);

        int promptBytes = prompt.getBytes(StandardCharsets.UTF_8).length;
        int requestBytes = requestBody.getBytes(StandardCharsets.UTF_8).length;

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("HTTP status: " + response.statusCode());

        JsonNode root = objectMapper.readTree(response.body());

        if (response.statusCode() < 200 || response.statusCode() >= 300 || root.has("error")) {
            String errorMessage = root.path("error").path("message").asText();

            throw new IllegalStateException("OpenRouter error: " + errorMessage);
        }

        JsonNode usage = root.path("usage");

        int promptTokens = usage.path("prompt_tokens").asInt();

        int completionTokens = usage.path("completion_tokens").asInt();

        int totalTokens = usage.path("total_tokens").asInt();

        System.out.println("Prompt tokens: " + promptTokens);

        System.out.println("Completion tokens: " + completionTokens);

        System.out.println("Total tokens: " + totalTokens);

        System.out.println("Prompt bytes: " + promptBytes);

        System.out.println("Request bytes: " + requestBytes);

        System.out.printf("Prompt size: %.2f KB%n", promptBytes / 1024.0);

        System.out.printf("Request size: %.2f KB%n", requestBytes / 1024.0);
        String answer = root.path("choices").path(0).path("message").path("content").asText();

        int answerBytes = answer.getBytes(StandardCharsets.UTF_8).length;

        int promptAndAnswerBytes = promptBytes + answerBytes;

        int requestAndAnswerBytes = requestBytes + answerBytes;

        System.out.println("AI answer:");
        System.out.println(answer);

        System.out.println("Answer bytes: " + answerBytes);

        System.out.println("Prompt + answer bytes: " + promptAndAnswerBytes);

        System.out.println("Request + answer bytes: " + requestAndAnswerBytes);
        return new TokenUsage(
            promptTokens,
            completionTokens,
            totalTokens,
            promptBytes,
            requestBytes,
            answerBytes,
            promptAndAnswerBytes,
            requestAndAnswerBytes
        );
    }

    private JsonNode sendToOpenRouter(String prompt) throws Exception {
        String requestBody = buildRequestBody(prompt);

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
            String errorMessage = root.path("error").path("message").asText();

            if (errorMessage == null || errorMessage.isBlank()) {
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

    private String toJsonString(String value) {
        try {
            return objectMapper.writeValueAsString(value == null ? "" : value);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot encode JSON string", ex);
        }
    }

    /**
     * Кодирует одну строку в простой TOON scalar.
     *
     * TOON требует кавычки, если строка содержит
     * специальные символы, переносы строк и т.д.
     *
     * Для теста безопаснее всегда использовать
     * JSON-style quoted string.
     */
    private String toToonString(String value) {
        return toJsonString(value);
    }

    private void saveFailedResult(NewsItem newsItem, String errorMessage) {
        AnalysisResult result = new AnalysisResult();

        result.setNewsItem(newsItem);

        result.setStatus(AnalysisStatus.FAILED);

        result.setSentiment(Sentiment.UNKNOWN);

        result.setAnalyzedAt(Instant.now());

        result.setErrorMessage(errorMessage);

        analysisResultRepository.save(result);
    }

    private double percentage(long saved, long original) {
        if (original == 0) {
            return 0.0;
        }

        return ((double) saved / original) * 100.0;
    }

    private double average(long total, int count) {
        if (count == 0) {
            return 0.0;
        }

        return (double) total / count;
    }

    private record TokenUsage(
        int promptTokens,
        int completionTokens,
        int totalTokens,
        int promptBytes,
        int requestBytes,
        int answerBytes,
        int promptAndAnswerBytes,
        int requestAndAnswerBytes
    ) {}
}
