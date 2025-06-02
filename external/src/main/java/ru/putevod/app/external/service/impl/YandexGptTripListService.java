package ru.putevod.app.external.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.putevod.app.external.client.PlannerClient;
import ru.putevod.app.external.dto.ai.YandexGptRequest;
import ru.putevod.app.external.exception.ServiceUnavailableException;
import ru.putevod.app.external.service.AiTripListService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Primary
@Slf4j
public class YandexGptTripListService implements AiTripListService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PlannerClient plannerClient;

    @Value("${app.yandex.gpt.api-key}")
    private String apiKey;

    @Value("${app.yandex.gpt.folder-id}")
    private String folderId;

    @Value("${app.yandex.gpt.api-url:https://llm.api.cloud.yandex.net/foundationModels/v1/completion}")
    private String apiUrl;

    @Value("${app.yandex.gpt.max-tokens:1000}")
    private String maxTokens;

    @Value("${app.yandex.gpt.temperature:0.7}")
    private Double temperature;

    private static final List<String> BANNED_TOPICS = Arrays.asList(
            "оружие", "наркотики", "насилие", "терроризм", "детская эксплуатация",
            "weapons", "drugs", "violence", "terrorism", "child exploitation"
    );

    @Override
    public List<String> generateTripListFromPrompt(String prompt, Map<String, Object> context) {
        if (prompt == null || prompt.trim().isEmpty()) {
            log.warn("Получен пустой запрос");
            return List.of("Ошибка: запрос не может быть пустым");
        }

        if (!isSafePrompt(prompt)) {
            log.warn("Обнаружен небезопасный запрос: {}", prompt);
            return List.of("Извините, запрос содержит запрещенную тематику.");
        }

        String systemPrompt = buildSystemPrompt(context);
        String userPrompt = buildUserPrompt(prompt, context);

        try {
            String response = callYandexGptApi(systemPrompt, userPrompt);
            List<String> items = parseListItems(response);

            if (items.isEmpty()) {
                log.warn("AI вернул пустой список");
                return List.of("Не удалось сгенерировать список. Пожалуйста, уточните запрос.");
            }

            return items;
        } catch (ServiceUnavailableException e) {
            log.error("Сервис недоступен: {}", e.getMessage(), e);
            return List.of("Сервис временно недоступен. Пожалуйста, попробуйте позже.");
        } catch (Exception e) {
            log.error("Ошибка при генерации списка с помощью AI: {}", e.getMessage(), e);
            return List.of("Произошла ошибка при генерации списка. Пожалуйста, попробуйте позже.");
        }
    }

    @Override
    public List<String> generateTripListFromTrip(Long tripId, Map<String, Object> context) {
        if (tripId == null) {
            log.warn("Получен null ID поездки");
            return List.of("Ошибка: ID поездки не может быть пустым");
        }

        Map<String, Object> tripDetails = plannerClient.getTripDetails(tripId);

        if (tripDetails.isEmpty()) {
            log.warn("Не удалось получить информацию о поездке с ID {}", tripId);
            return List.of("Не удалось получить информацию о поездке. Проверьте ID и попробуйте снова.");
        }

        Map<String, Object> tripContext = new HashMap<>(context);
        tripContext.putAll(tripDetails);

        StringBuilder promptBuilder = new StringBuilder("Составь список вещей для поездки");

        if (tripDetails.containsKey("title")) {
            promptBuilder.append(" \"").append(tripDetails.get("title")).append("\"");
        }

        if (tripDetails.containsKey("country") || tripDetails.containsKey("city")) {
            promptBuilder.append(" в ");

            if (tripDetails.containsKey("country")) {
                promptBuilder.append(tripDetails.get("country"));

                if (tripDetails.containsKey("city")) {
                    promptBuilder.append(", ");
                }
            }

            if (tripDetails.containsKey("city")) {
                promptBuilder.append(tripDetails.get("city"));
            }
        }

        if (tripDetails.containsKey("start_date") && tripDetails.containsKey("end_date")) {
            promptBuilder.append(" с ").append(tripDetails.get("start_date"))
                    .append(" по ").append(tripDetails.get("end_date"));
        }

        return generateTripListFromPrompt(promptBuilder.toString(), tripContext);
    }

    @Override
    public List<String> generateTripListFromTemplate(Long templateId, Map<String, Object> context) {
        if (templateId == null) {
            log.warn("Получен null ID шаблона");
            return List.of("Ошибка: ID шаблона не может быть пустым");
        }

        // Получаем информацию о шаблоне
        Map<String, Object> templateDetails = plannerClient.getTemplateDetails(templateId);

        if (templateDetails.isEmpty()) {
            log.warn("Не удалось получить информацию о шаблоне с ID {}", templateId);
            return List.of("Не удалось получить информацию о шаблоне. Проверьте ID и попробуйте снова.");
        }

        List<String> templateItems = plannerClient.getTemplateItems(templateId);

        if (!templateItems.isEmpty()) {
            Map<String, Object> templateContext = new HashMap<>(context);
            templateContext.putAll(templateDetails);

            StringBuilder promptBuilder = new StringBuilder();

            if (templateDetails.containsKey("title")) {
                promptBuilder.append("На основе шаблона \"").append(templateDetails.get("title")).append("\" ");
            } else {
                promptBuilder.append("На основе существующего шаблона ");
            }

            promptBuilder.append("создай персонализированный список вещей для поездки, включающий следующие элементы:\n\n");

            for (String item : templateItems) {
                promptBuilder.append("- ").append(item).append("\n");
            }

            promptBuilder.append("\nДополни этот список с учетом информации о поездке и сделай его более персонализированным.");

            return generateTripListFromPrompt(promptBuilder.toString(), templateContext);
        } else {
            Map<String, Object> templateContext = new HashMap<>(context);
            templateContext.putAll(templateDetails);

            StringBuilder promptBuilder = new StringBuilder("Составь список вещей для поездки");

            if (templateDetails.containsKey("category")) {
                promptBuilder.append(" типа \"").append(templateDetails.get("category")).append("\"");
            }

            if (templateDetails.containsKey("title")) {
                promptBuilder.append(" на основе шаблона \"").append(templateDetails.get("title")).append("\"");
            }

            if (templateDetails.containsKey("description") && templateDetails.get("description") != null) {
                promptBuilder.append(". Описание шаблона: ").append(templateDetails.get("description"));
            }

            return generateTripListFromPrompt(promptBuilder.toString(), templateContext);
        }
    }

    @Override
    public boolean isSafePrompt(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return false;
        }

        String lowerPrompt = prompt.toLowerCase();

        for (String bannedTopic : BANNED_TOPICS) {
            if (lowerPrompt.contains(bannedTopic.toLowerCase())) {
                log.warn("Обнаружена запрещенная тема в запросе: {}", bannedTopic);
                return false;
            }
        }

        return true;
    }

    private String buildSystemPrompt(Map<String, Object> context) {
        StringBuilder systemPrompt = new StringBuilder();
        systemPrompt.append("Ты - помощник для составления списков для путешествий. ");
        systemPrompt.append("Твоя задача - составить детальный список элементов для поездки ");
        systemPrompt.append("на основе запроса пользователя и доступной информации о поездке. ");
        systemPrompt.append("Отвечай только списком элементов, без вводных слов и объяснений. ");
        systemPrompt.append("Каждый элемент должен начинаться с новой строки. ");
        systemPrompt.append("Не нумеруй элементы списка. ");

        if (context != null && !context.isEmpty()) {
            systemPrompt.append("Используй следующую информацию при составлении списка: ");

            if (context.containsKey("duration")) {
                systemPrompt.append("Продолжительность поездки: ").append(context.get("duration")).append(". ");
            }

            if (context.containsKey("destination")) {
                systemPrompt.append("Место назначения: ").append(context.get("destination")).append(". ");
            }

            if (context.containsKey("season")) {
                systemPrompt.append("Сезон: ").append(context.get("season")).append(". ");
            }

            if (context.containsKey("country") || context.containsKey("city")) {
                systemPrompt.append("Место поездки: ");

                if (context.containsKey("country")) {
                    systemPrompt.append(context.get("country"));

                    if (context.containsKey("city")) {
                        systemPrompt.append(", ");
                    }
                }

                if (context.containsKey("city")) {
                    systemPrompt.append(context.get("city"));
                }

                systemPrompt.append(". ");
            }

            if (context.containsKey("start_date") && context.containsKey("end_date")) {
                systemPrompt.append("Даты поездки: с ").append(context.get("start_date"))
                        .append(" по ").append(context.get("end_date")).append(". ");
            }
        }

        return systemPrompt.toString();
    }

    private String buildUserPrompt(String userInput, Map<String, Object> context) {
        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("Пожалуйста, составь список для поездки: ").append(userInput);

        if (context != null && context.containsKey("additionalInfo")) {
            userPrompt.append("\n\nДополнительная информация: ").append(context.get("additionalInfo"));
        }

        return userPrompt.toString();
    }

    private String callYandexGptApi(String systemPrompt, String userPrompt) throws Exception {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new ServiceUnavailableException("API ключ для YandexGPT не настроен");
        }

        if (folderId == null || folderId.isEmpty()) {
            throw new ServiceUnavailableException("Folder ID для YandexGPT не настроен");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Api-Key " + apiKey);
        headers.set("x-folder-id", folderId);

        List<YandexGptRequest.Message> messages = new ArrayList<>();
        messages.add(YandexGptRequest.Message.builder()
                .role("system")
                .text(systemPrompt)
                .build());
        messages.add(YandexGptRequest.Message.builder()
                .role("user")
                .text(userPrompt)
                .build());

        String modelUri = String.format("gpt://%s/yandexgpt/latest", folderId);

        YandexGptRequest requestBody = YandexGptRequest.builder()
                .modelUri(modelUri)
                .completionOptions(YandexGptRequest.CompletionOptions.builder()
                        .stream(false)
                        .temperature(temperature)
                        .maxTokens(maxTokens)
                        .build())
                .messages(messages)
                .build();

        HttpEntity<YandexGptRequest> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            String responseJson = restTemplate.postForObject(apiUrl, requestEntity, String.class);

            if (responseJson == null || responseJson.isEmpty()) {
                throw new ServiceUnavailableException("Пустой ответ от API YandexGPT");
            }

            JsonNode rootNode = objectMapper.readTree(responseJson);
            JsonNode resultNode = rootNode.path("result");

            if (resultNode.isMissingNode()) {
                throw new ServiceUnavailableException("Отсутствуют результаты в ответе от API YandexGPT");
            }

            JsonNode alternativesNode = resultNode.path("alternatives");

            if (alternativesNode.isMissingNode() || alternativesNode.isEmpty()) {
                throw new ServiceUnavailableException("Отсутствуют альтернативы в ответе от API YandexGPT");
            }

            return alternativesNode.path(0).path("message").path("text").asText();
        } catch (RestClientException e) {
            log.error("Ошибка при вызове YandexGPT API: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Ошибка при вызове AI API: " + e.getMessage(), e);
        }
    }

    private List<String> parseListItems(String aiResponse) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<String> lines = Arrays.asList(aiResponse.split("\\r?\\n"));

        return lines.stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .map(line -> {
                    // Удаляем маркеры списка (-, *, •, цифры с точкой)
                    if (line.matches("^[\\-\\*•]\\s+.*$")) {
                        return line.replaceFirst("^[\\-\\*•]\\s+", "");
                    } else if (line.matches("^\\d+\\.\\s+.*$")) {
                        return line.replaceFirst("^\\d+\\.\\s+", "");
                    }
                    return line;
                })
                .collect(Collectors.toList());
    }
} 